package overlay_matrix_graph;

import location_iq.ExcellReader;
import location_iq.Exceptions.CellTypeException;
import location_iq.ExternalCSVDump;
import location_iq.Point;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.Exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.quadTree.QuadTreeNode;
import util.HeartDistance;

import java.io.*;
import java.util.Map;

/**
 * This class works as an interface that interact with the real overlay graph, manages the creation or the
 * load of the graph, in case of creation manages the dump of the distance.
 * Manages the supporters and the route requests
 */
public class MatrixOverlayGraphManager {
    private static final String PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private static final String DEPOT_CUSTOMER = "GHDumpFolder\\Depot-Customer\\";
    private static final String DEPOT_DEPOT = "GHDumpFolder\\Depot-depot\\";
    private static final String OVERLAY_GRAPH = "OverlayGraph\\OverlayGraph";
    private static final String OVERLAY_GRAPH_QUADTREE = "OverlayGraph\\QuadTree";
    private static final String POINTS_GEOCODE = "geocodedAddresses.xlsx";

    private MatrixOverlayGraph graph;
    private ExternalCSVDump dump;

    /**
     * If a file with the graph exists this will be loaded, otherwise the graph will be created
     * parsing the dump file and then will be written
     */
    public void loadOrCreateGraph() {
        if(new File(PATH+OVERLAY_GRAPH).exists())
            loadGraph();
        else {
            System.out.println("No file with the graph exists");
            createGraph();
        }
        //graph.print();
    }

    /**
     * This function parse a specific dump file with precomputed distnces and creates the OverlayGraph
     */
    private void createGraph() {
        System.out.println("The graph will be created from scratch");
        graph = new MatrixOverlayGraph();
        parse(PATH + DEPOT_DEPOT);
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
        String code = dumpName.replace(".csv","");
        Source source = new Source();
        Map response = dump.parseNext();
        while (response != null) {
            source.addNewPath(response);
            response = dump.parseNext();
        }
        try {
            source.setNodeInfo(parseNodeInfo(code));
            graph.put(code,source);
        } catch (IOException e) {
            System.out.println("The point " + code + " has not be put into the graph for this error: ");
            e.printStackTrace();
        }
    }

    /**
     * Parse the file untill the code will found and take the node's information: code, latitude
     * and longitude
     * @param code code to be finded into the file
     * @return Point containing the information of the source of all the parsed routes
     * @throws IOException if will be a problem reading the file
     */
    private Point parseNodeInfo(String code) throws IOException {
        ExcellReader exr = new ExcellReader(PATH + POINTS_GEOCODE);
        exr.setSheetWithIndex(1).initializeIterator(); //TODO no be hardcoded
        while(exr.nextRow()) {
            try {
                if(exr.getID().equalsIgnoreCase(code))
                    return new Point(code, exr.getLatitude(), exr.getLongitude());
            } catch(CellTypeException e) {
                System.err.println("Exception reading " + code + "'s data");
            }
        }
        throw new IOException(code + "'s data not founded");
    }

    /**
     * Write the graph into an external file
     */
    private void saveGraph() {
        try(FileOutputStream fileOut = new FileOutputStream(PATH + OVERLAY_GRAPH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("The Graph was successfully written to a file");
    }

    /**
     * Write the supporters into an external file
     */
    private void saveSupporters() {
        try(FileOutputStream fileOut = new FileOutputStream(PATH + OVERLAY_GRAPH_QUADTREE);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph.getSupporters());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Graph's supporters were successfully written to a file");
    }

    /**
     * Read the graph from the external file
     */
    private void loadGraph() {
        System.out.println("The Graph will be loaded from an external file");
        try(FileInputStream fis = new FileInputStream(PATH + OVERLAY_GRAPH);
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
        try(FileInputStream fis = new FileInputStream(PATH + OVERLAY_GRAPH_QUADTREE);
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
         * also if the neighbour is the same could be a problem both the the hashmap and for the result
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
        if(response.getInitialPath()) {
            response.computeTimeWithSpeedProfile(new HeartDistance().
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
            response.computeTimeWithSpeedProfile(new HeartDistance().
                    calculate(response.getDestinationNeighbour(), response.getDestination()));
        }
        return response;
    }

    public void printGraph() {
        System.out.println("Our Graph is: ");
        graph.print();
    }


}
