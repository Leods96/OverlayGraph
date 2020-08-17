package overlay_matrix_graph;

import location_iq.ExternalCSVDump;
import location_iq.Point;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.supporters.LocalityGraph;
import overlay_matrix_graph.supporters.Supporter;
import util.AngleCalculator;
import util.HeartDistance;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 //TODO maybe it is possible to write only the graph after that the supporters are created and not do a lot of write and read for each supporters
/**
 * This class works as an interface that interact with the real overlay graph, manages the creation or the
 * load of the graph, in case of creation manages the dump of the distance.
 * Manages the supporters and the route requests
 */
public class MatrixOverlayGraphManager {
    private static final String MAIN_PATH = "OverlayGraph";
    private static final String OVERLAY_GRAPH = "OverlayGraph\\OverlayGraph";
    private static final String KDTREE_SUPPORTER_PATH = "OverlayGraph\\KdTree";
    //private static final String LOCALITY_GRAPH_PATH = "OverlayGraph\\LocalityGraph";
    private static final String LINEAR_SUPPORTER = "OverlayGraph\\Linear";
    private static final boolean angleNeighboursHint = true;
    private static final int THRESHOLD_NEIGHBOUR_DISTANCE = 100000; //100 km
    /**
     * This threshold represent the maximum allowed distance (in meters) between the nearest neighbour
     * and the other possible neighbours
     * TODO make it a settable parameter
     */
    private static final double NEIGHBOURS_THRESHOLD = 1500;
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

    private boolean kdTreeSupporterActived = false;

    /**
     * set the directory of the graph both to read and write it
     * @param graphPath
     */
    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
        if(graphPath == null)
            System.err.println("Null path");
        else {
            File dir = new File(graphPath + MAIN_PATH);
            if(!dir.exists()) {
                dir.mkdir();
                //TODO questo va fatto nella creazione del grafo non nel settaggio del path
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

    public void setUseOfKdTree() {
        this.kdTreeSupporterActived = true;
    }
    /**
     * Set the path and call loadOrCreateGraph
     */
    public void loadOrCreateGraph(String graphPath, String dumpPath) {
        setGraphPath(graphPath);
        setDumpsDirectoryToCreateGraph(dumpPath);
        loadOrCreateGraph();
    }

    public void loadOrCreateGraph(String graphPath, String dumpPath, boolean kdTreeSupporterActive) {
        if(kdTreeSupporterActive)
            setUseOfKdTree();
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
        System.out.println("The overlay graph will be created from scratch");
        graph = new MatrixOverlayGraph();
        parse(dumpPath);
        saveGraph();
        graph.createSupporters(kdTreeSupporterActived);
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
            System.out.println("The overlay graph was successfully written to a file");
        } catch (Exception ex) {
            System.out.println("Error writing the overlay graph");
            ex.printStackTrace();
        }
    }

    /**
     * Write the supporters into an external file
     */
    private void saveSupporters() {
        try {
            try (FileOutputStream fileOut = new FileOutputStream(graphPath + (kdTreeSupporterActived ? KDTREE_SUPPORTER_PATH : LINEAR_SUPPORTER));
                 ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
                objectOut.writeObject(graph.getSupporters());
            }
        } catch (Exception ex) {
            System.out.println("Error writing the overlay graph's supporters");
            ex.printStackTrace();
        }
        System.out.println("Overlay graph's supporters were successfully written to a file");
    }

    /**
     * Read the graph from the external file
     */
    public void loadGraph() {
        System.out.println("The overlay graph will be loaded from an external file");
        try(FileInputStream fis = new FileInputStream(graphPath + OVERLAY_GRAPH);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph = (MatrixOverlayGraph) ois.readObject();
            System.out.println("Overlay graph loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
        loadSupporters();
    }

    /**
     * Read the supporters from the external file
     */
    public void loadSupporters() {
        try {
            try (FileInputStream fis = new FileInputStream(graphPath + (kdTreeSupporterActived ? KDTREE_SUPPORTER_PATH : LINEAR_SUPPORTER));
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                graph.setSupporter((Supporter) ois.readObject());
            }
        } catch (Exception e) {
            System.err.println("Error Loading the supporters");
            e.printStackTrace();
        }
        System.out.println("Overlay graph's supporters loaded");
    }

    /**
     * If one or both of the points is part of the overlay graph the research will be simplified by
     * using only the graph's points
     * Compute the response as a combination of route: the vicinities of the graph plus the precomputed
     * distance over the overlay graph
     * @param fromPoint origin of the route
     * @param toPoint destination of the route
     * @return An OverlayResponse with all the information of the route
     * @throws NodeCodeNotInOverlayGraphException Exception raised if the graph will not contain the code of
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse route(Point fromPoint, Point toPoint) throws NodeCodeNotInOverlayGraphException {
        ArrayList<Point> originNeighbours = null;
        ArrayList<Point> destinationNeighbours = null;
        OverlayResponse response = new OverlayResponse(fromPoint, toPoint);

        try {
            response.setOriginCode(graph.pointPresentIntoGraph(fromPoint).getCode());
            originNeighbours = new ArrayList<>();
            originNeighbours.add(fromPoint);
        } catch (NodeNotInOverlayGraphException e) {
            originNeighbours = new ArrayList<>(graph.searchNeighbour(fromPoint));
            if(angleNeighboursHint)
                originNeighbours.addAll(graph.
                        searchNeighbourWithAngleHint(fromPoint, AngleCalculator.getAngle(fromPoint, toPoint)));
            response.setStartingStep();
        }
        try {
            response.setDestinationCode(graph.pointPresentIntoGraph(toPoint).getCode());
            destinationNeighbours = new ArrayList<>();
            destinationNeighbours.add(toPoint);
        } catch (NodeNotInOverlayGraphException e) {
            destinationNeighbours = new ArrayList<>(graph.searchNeighbour(toPoint));
            if(angleNeighboursHint)
                destinationNeighbours.addAll(graph.
                        searchNeighbourWithAngleHint(toPoint, AngleCalculator.getAngle(toPoint, fromPoint)));
            response.setFinalStep();
        }
        return neighboursComputation(response, originNeighbours, destinationNeighbours);
    }

    /**
     * Take the two sets of neighbours as input, if there is an intersection between them the computation
     * will be based on Haversine formula
     * If there is no intersection between the neighbours the two sets will be filtered with respect to
     * the first element of each set (representing the nearest neighbour) based on the defined threshold
     * and then all the possible route are computed with the overlay graph and the best one will be passed
     * to the routeComposition()
     * @param response OverlayResponse we are working with
     * @param originNeighbours set of origin's nearest neighbours
     * @param destinationNeighbours set of origin's nearest neighbours
     * @return An OverlayResponse with all the information of the route
     * @throws NodeCodeNotInOverlayGraphException Exception raised if the graph will not contain the code of
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse neighboursComputation(OverlayResponse response, List<Point> originNeighbours, List<Point> destinationNeighbours ) throws NodeCodeNotInOverlayGraphException {
        //Check the intersection between the neighbours
        for (Point p : originNeighbours) {
            if (destinationNeighbours.contains(p)) {
                System.out.println("Neighbours intersection");
                response.removeOverlayFromResponse();
                return routeComposition(response, null);
            }
        }
        //Computation of the best overlay path
        originNeighbours = neighboursFiltering(originNeighbours);
        destinationNeighbours = neighboursFiltering(destinationNeighbours);
        Point origin = null;
        Point destination = null;
        double overlayDistance = Double.MAX_VALUE;
        RouteInfo middlePath = null;
        for(Point p1 : originNeighbours) {
            for (Point p2 : destinationNeighbours) {
                RouteInfo resp = graph.route(p1.getCode(), p2.getCode());
                if(resp.getDistance() < overlayDistance) {
                    origin = p1;
                    destination = p2;
                    overlayDistance = resp.getDistance();
                    middlePath = resp;
                }
            }
        }
        response.setOriginNeighbour(origin);
        response.setDestinationNeighbour(destination);
        return routeComposition(response, middlePath);
    }

    /**
     * Remove from the input list all the points with a distance with respect to the first element
     * above the threshold
     * @param points Ordered list of points to be filtered
     * @return A filtered list of points
     */
    private List<Point> neighboursFiltering(List<Point> points) {
        Point nearest = points.get(0);
        HeartDistance calculator = new HeartDistance();
        return points.stream().filter(p -> calculator.calculate(p,nearest) < NEIGHBOURS_THRESHOLD).collect(Collectors.toList());
    }

    /**
     * If the middlePath flag is false then the Haversine distance is computed otherwise the response
     * is composed
     * @param response OverlayResponse we are working with
     * @param overlayDistance precomputed distance of the overlay graph
     * @return An OverlayResponse with all the information of the route
     * @throws NodeCodeNotInOverlayGraphException Exception raised if the graph will not contain the code of
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse routeComposition(OverlayResponse response, RouteInfo overlayDistance) throws NodeCodeNotInOverlayGraphException {
        HeartDistance distCalculator = new HeartDistance();
        //FARE: check su middel path
        if(!response.getMiddlePath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getOrigin(),response.getDestination()));
            return response;
        }
        if(response.getInitialPath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getOrigin(), response.getOriginNeighbour()));
        }
        //FARE: rimuovere e mettere rsultato pre funzione
        if(response.getMiddlePath()) {
            response.concat(overlayDistance);
        }
        if(response.getFinalPath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getDestinationNeighbour(), response.getDestination()));
        }
        return response;
    }

    public void printGraph() {
        System.out.println("Our overlay graph is: ");
        graph.print();
    }


}
