package overlay_matrix_graph;

import location_iq.ExternalCSVDump;
import location_iq.Point;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.quadTree.QuadTreeNode;
import util.HeartDistance;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class works as an interface that interact with the real overlay graph, manages the creation or the
 * load of the graph, in case of creation manages the dump of the distance.
 * Manages the supporters and the route requests
 */
public class MatrixOverlayGraphManager {
    private static final String OVERLAY_GRAPH = "OverlayGraph\\OverlayGraph";
    private static final String OVERLAY_GRAPH_QUADTREE = "OverlayGraph\\QuadTree";
    private static final String POINTS_GEOCODE = "geocodedAddresses.xlsx";

    private MatrixOverlayGraph graph;
    private ExternalCSVDump dump;
    /**
     * The path where save and read the graph
     */
    private String graphPath = null;
    /**
     * The files from which create the graph
     */
    private String dumpPath = null;

    /**
     * set the directory of the graph both to read and write it
     * @param graphPath
     */
    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
        if(graphPath == null)
            System.err.println("Null path");
        else {
            File dir = new File(graphPath + "OverlayGraph");
            if(!dir.exists()) {
                dir.mkdir();
                System.out.println("Directory to save the graph created");
            }
        }
    }

    /**
     * set the path of the dumps file from which read the nodes and create the graph
     * @param dumpPath
     */
    public void setDumpsDirectoryToCreateGraph(String dumpPath) {
        this.dumpPath = dumpPath;
    }

    /**
     * Set the path and call loadOrCreateGraph
     */
    public void loadOrCreateGraph(String graphPath, String dumpPath) {
        setGraphPath(graphPath);
        setDumpsDirectoryToCreateGraph(dumpPath);
        loadOrCreateGraph();
    }

    /**
     * If a file with the graph exists this will be loaded, otherwise the graph will be created
     * parsing the dump file and then will be written
     */
    public void loadOrCreateGraph() {
        if(new File(graphPath + OVERLAY_GRAPH).exists())
            loadGraph();
        else {
            System.out.println("No file with the graph exists");
            createGraph();
        }
        //graph.print();
    }

    /**
     * Function that parse a specific dump file with precomputed distances and creates the OverlayGraph
     */
    public void createGraph() {
        System.out.println("The graph will be created from scratch");
        graph = new MatrixOverlayGraph();
        parse(dumpPath);
        saveGraph();
        graph.createSupporters();
        saveSupporters();
    }

    /**
     * Parse all the file at the specified path, use the name of the file as code for points
     * @param path address of the directory with the dump files
     */
    private void parse(String path) {
        dump = new ExternalCSVDump(path, true);
        File folder = new File(path);
        File[] files = folder.listFiles();
        for(File f : files)
            try {
                parseDump(f.getName());
            } catch (IOException e) {
                System.err.println("IOException parsing file " + f.getName());
            }
    }

    /**
     * Parse all the file and put all the path in the object Source that will contains all the routes
     * with this node as Origin and then put this object in the first graph hashMap identified by
     * the node's code
     * @param dumpName File to be read with inside the information of all the routes from a specific
     * point and all the others, the name of the file is equal to te source point code.
     * @throws IOException Exception raised if there are problems with the dump file
     */
    private void parseDump(String dumpName) throws IOException {
        dump.createParserFile(dumpName);
        System.out.println("Processing the file: " +  dumpName);
        Source source = new Source();
        source.setNodeInfo(parseNodeInfo());
        String code = source.getNodeInfo().getCode();
        Map response = dump.parseNext();
        while (response != null) {
            source.addNewPath(response);
            response = dump.parseNext();
        }
        if(source.isRoutesEmpty())
            System.err.println("Node " + code + " has no routes, discarded from the graph");
        else
            graph.put(code,source);
    }

    /**
     * Parse the file until the code will found and take the node's information: code, latitude
     * and longitude
     * @return Point containing the information of the source of all the parsed routes
     */
    private Point parseNodeInfo() {
        HashMap<String, Object> m = new HashMap<>(dump.parseSourceInfo());
        return new Point((String) m.get("code"),
                Double.parseDouble((String)m.get("Latitude")), Double.parseDouble((String)m.get("Longitude")));
    }

    /**
     * Write the graph into an external file
     */
    private void saveGraph() {
        try(FileOutputStream fileOut = new FileOutputStream(graphPath + OVERLAY_GRAPH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph);
            System.out.println("The Graph was successfully written to a file");
        } catch (Exception ex) {
            System.out.println("Error writing the graph");
            ex.printStackTrace();
        }
    }

    /**
     * Write the supporters into an external file
     */
    private void saveSupporters() {
        try(FileOutputStream fileOut = new FileOutputStream(graphPath + OVERLAY_GRAPH_QUADTREE);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph.getSupporters());
            System.out.println("Graph's supporters were successfully written to a file");
        } catch (Exception ex) {
            System.out.println("Error writing the graph's supporters");
            ex.printStackTrace();
        }
    }

    /**
     * Read the graph from the external file
     */
    public void loadGraph() {
        System.out.println("The Graph will be loaded from an external file");
        try(FileInputStream fis = new FileInputStream(graphPath + OVERLAY_GRAPH);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph = (MatrixOverlayGraph) ois.readObject();
            System.out.println("Graph loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSupporters();
    }

    /**
     * Read the supporters from the external file
     */
    public void loadSupporters() {
        try(FileInputStream fis = new FileInputStream(graphPath + OVERLAY_GRAPH_QUADTREE);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph.setQuadTreeSupport((QuadTreeNode) ois.readObject());
            System.out.println("supporters loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If one or both of the points is part of the overlay graph the research will be simplified by the using
     * only the graph's points
     * Compute the response as a combination of route: the vicinities of the graph plus the precomputed
     * distance over the overlay graph
     * @param fromPoint origin of the route
     * @param toPoint destination of the route
     * @return An OverlayResponse with all the information of the route
     * @throws NodeCodeNotInOverlayGraphException Exception raised if the graph will not contain the code of
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse route(Point fromPoint, Point toPoint) throws NodeCodeNotInOverlayGraphException {
        OverlayResponse response = new OverlayResponse(fromPoint, toPoint);
        /**
         * TODO check if we have to use the graph (if the points are near may we can use different approach
         * as the haversine) in case use the method removeOverlayFromResponse()
         * also if the neighbour is the same could be a problem both for the hashmap and for the result
         */
        try {
            response.setOriginCode(graph.pointPresentIntoGraph(fromPoint).getCode());
        } catch (NodeNotInOverlayGraphException e) {
            response.setOriginNeighbour(graph.searchNeighbour(fromPoint));
            response.setStartingStep();
        }
        try {
            response.setDestinationCode(graph.pointPresentIntoGraph(toPoint).getCode());
        } catch (NodeNotInOverlayGraphException e) {
            response.setDestinationNeighbour(graph.searchNeighbour(toPoint));
            response.setFinalStep();
        }
        return routeComposition(response);
    }

    public OverlayResponse routeComposition(OverlayResponse response) throws NodeCodeNotInOverlayGraphException {
        HeartDistance distCalculator = new HeartDistance();
        if(response.getOriginNeighbour() == response.getDestinationNeighbour()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getOrigin(),response.getDestination()));
            return response;
        }
        if(response.getInitialPath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getOrigin(), response.getOriginNeighbour()));
        }
        if(response.getMiddlePath()) {
            if(response.getInitialPath() && response.getFinalPath()) {
                response.concat(graph.route(response.getOriginNeighbour().getCode(),
                        response.getDestinationNeighbour().getCode()));
            } else if (response.getInitialPath()){
                response.concat(graph.route(response.getOriginNeighbour().getCode(),
                        response.getDestination().getCode()));
            } else if(response.getFinalPath()) {
                response.concat(graph.route(response.getOrigin().getCode(),
                        response.getDestinationNeighbour().getCode()));
            } else {
                response.concat(graph.route(response.getOrigin().getCode(),
                        response.getDestination().getCode()));
            }
        }
        if(response.getFinalPath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getDestinationNeighbour(), response.getDestination()));
        }
        return response;
    }

    public void printGraph() {
        System.out.println("Our Graph is: ");
        graph.print();
    }


}
