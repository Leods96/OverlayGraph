package overlay_matrix_graph;

import input_output.ExternalCSVDump;
import objects.ParamsObject;
import objects.Point;
import overlay_matrix_graph.exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.supporters.NeighbourResponse;
import overlay_matrix_graph.supporters.Supporter;
import util.AngleCalculator;
import util.HeartDistance;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
 //TODO maybe it is possible to write only the graph after that the supporters are created and not do a lot of write and read for each supporters
/**
 * This class works as an interface that interact with the real overlay graph, manages the creation or the
 * load of the graph, in case of creation manages the dump of the distance.
 * Manages the supporters and the route requests
 */
public class MatrixOverlayGraphManager {
    private static final String OVERLAY_GRAPH = "\\OverlayGraph";
    private static final String KDTREE_SUPPORTER_PATH = "\\KdTree";
    //private static final String LOCALITY_GRAPH_PATH = "OverlayGraph\\LocalityGraph";
    private static final String LINEAR_SUPPORTER = "\\Linear";
    /**
     * This parameter define the use of the angle hint research during the NNR
     */
    private boolean ANGLE_NEIGHBOURS_HINT = true;

    private boolean BEST_PATH_CHOSE_OVER_OVERLAY_ONLY = false;
    /**
     * This threshold represents the maximum allowed distance (in meters) between the nearest neighbour
     * and the other possible neighbours
     */
    private double NEIGHBOURS_THRESHOLD = 1500;
    /**
     * This threshold represents the maximum allowed distance (in meters) between the overlay point and his
     * neighbours
     */
    private double THRESHOLD_NEIGHBOUR_DISTANCE = 100000; //100 km

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
    }

    public void setParams(ParamsObject po) {
        if (po == null)
            return;
        if(po.isAngleHint() != null) this.ANGLE_NEIGHBOURS_HINT = po.isAngleHint();
        if(po.isOverlayOnly() != null) this.BEST_PATH_CHOSE_OVER_OVERLAY_ONLY = po.isOverlayOnly();
        if(po.getNeighbourThreshold() != null) this.NEIGHBOURS_THRESHOLD = po.getNeighbourThreshold();
        if(po.getNeighbourDistance() != null) this.THRESHOLD_NEIGHBOUR_DISTANCE = po.getNeighbourDistance();
        graph.setParams(po);
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
    public void loadOrCreateGraph(String graphPath, String dumpPath) throws IOException, ClassNotFoundException {
        setGraphPath(graphPath);
        setDumpsDirectoryToCreateGraph(dumpPath);
        loadOrCreateGraph();
    }

    public void loadOrCreateGraph(String graphPath, String dumpPath, boolean kdTreeSupporterActive) throws IOException, ClassNotFoundException {
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
    public void loadOrCreateGraph() throws IOException, ClassNotFoundException{
        if(graphPath != null && new File(graphPath).exists())
            loadGraph();
        else {
            System.out.println("No file with the graph exists");
            createGraph();
        }
    }

    /**
     * Function that parse a specific dump file with precomputed distances and creates the OverlayGraph
     */
    public void createGraph() throws IOException{
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
        dump = new ExternalCSVDump(path);
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
    private void saveGraph() throws IOException {
        new File(graphPath).mkdir();
        try(FileOutputStream fileOut = new FileOutputStream(graphPath + OVERLAY_GRAPH);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut))
        {
            objectOut.writeObject(graph);
            System.out.println("The overlay graph was successfully written to a file");
        } catch (IOException e) {
            new File(graphPath).delete();
            throw e;
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
    public void loadGraph() throws IOException, ClassNotFoundException{
        System.out.println("The overlay graph will be loaded from an external file");
        try(FileInputStream fis = new FileInputStream(graphPath + OVERLAY_GRAPH);
            ObjectInputStream ois = new ObjectInputStream(fis))
        {
            graph = (MatrixOverlayGraph) ois.readObject();
            System.out.println("Overlay graph loaded");
        }
        loadSupporters();
    }

    /**
     * Read the supporters from the external file
     */
    public void loadSupporters() {
        try {
            this.kdTreeSupporterActived = Arrays.stream(Objects.requireNonNull(new File(graphPath).listFiles())).
                map(File::getName).filter(s -> s.compareTo("KdTree") == 0).count() == 1;
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
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse route(Point fromPoint, Point toPoint) {
        ArrayList<NeighbourResponse> originNeighbours = null;
        ArrayList<NeighbourResponse> destinationNeighbours = null;
        OverlayResponse response = new OverlayResponse(fromPoint, toPoint);
        try {
            response.setOriginCode(graph.pointPresentIntoGraph(fromPoint).getCode());
            originNeighbours = new ArrayList<>();
            originNeighbours.add(new NeighbourResponse(fromPoint, 0.0));
        } catch (NodeNotInOverlayGraphException e) {
            //TODO check why we do the add all if the hint is true?
            originNeighbours = new ArrayList<>(graph.searchNeighbour(fromPoint));
            if(ANGLE_NEIGHBOURS_HINT)
                originNeighbours.addAll(graph.
                        searchNeighbourWithAngleHint(fromPoint, AngleCalculator.getAngle(fromPoint, toPoint)));
            response.setStartingStep();
        }
        try {
            response.setDestinationCode(graph.pointPresentIntoGraph(toPoint).getCode());
            destinationNeighbours = new ArrayList<>();
            destinationNeighbours.add(new NeighbourResponse(toPoint, 0.0));
        } catch (NodeNotInOverlayGraphException e) {
            destinationNeighbours = new ArrayList<>(graph.searchNeighbour(toPoint));
            if(ANGLE_NEIGHBOURS_HINT)
                destinationNeighbours.addAll(graph.
                        searchNeighbourWithAngleHint(toPoint, AngleCalculator.getAngle(toPoint, fromPoint)));
            response.setFinalStep();
        }
        if(BEST_PATH_CHOSE_OVER_OVERLAY_ONLY)
            return neighboursOverlayComputation(response, originNeighbours, destinationNeighbours);
        else
            return neighboursTotalComputation(response, originNeighbours, destinationNeighbours);
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
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse neighboursOverlayComputation(OverlayResponse response, List<NeighbourResponse> originNeighbours, List<NeighbourResponse> destinationNeighbours ) {
        //Check the intersection between the neighbours
        if (checkIntersectionBetweenNeighbours(originNeighbours, destinationNeighbours)) {
                System.out.println("Neighbours intersection");
                response.removeOverlayFromResponse();
                return routeComposition(response, null, null, null);
        }
        //Computation of the best overlay path
        originNeighbours = neighboursFiltering(originNeighbours);
        destinationNeighbours = neighboursFiltering(destinationNeighbours);
        NeighbourResponse origin = null;
        NeighbourResponse destination = null;
        double overlayDistance = Double.MAX_VALUE;
        RouteInfo middlePath = null;
        System.out.println("Origin neighbours: " + originNeighbours + "\nDestination neighbours: " + destinationNeighbours);
        for(NeighbourResponse n1 : originNeighbours) {
            for (NeighbourResponse n2 : destinationNeighbours) {
                RouteInfo resp = graph.route(n1.getPoint().getCode(), n2.getPoint().getCode());
                if(resp.getDistance() < overlayDistance) {
                    origin = n1;
                    destination = n2;
                    overlayDistance = resp.getDistance();
                    middlePath = resp;
                }
            }
        }
        response.setOriginNeighbour(origin.getPoint());
        response.setDestinationNeighbour(destination.getPoint());
        return routeComposition(response, middlePath, origin.getDistance(), destination.getDistance());
    }

    public OverlayResponse neighboursTotalComputation(OverlayResponse response, List<NeighbourResponse> originNeighbours, List<NeighbourResponse> destinationNeighbours ) {
        originNeighbours = originNeighbours.stream().filter(n -> n.getDistance() < THRESHOLD_NEIGHBOUR_DISTANCE).collect(Collectors.toList());
        destinationNeighbours = destinationNeighbours.stream().filter(n -> n.getDistance() < THRESHOLD_NEIGHBOUR_DISTANCE).collect(Collectors.toList());

        if(checkIntersectionBetweenNeighbours(originNeighbours, destinationNeighbours)) {
            System.out.println("Neighbours intersection");
            response.removeOverlayFromResponse();
            return routeComposition(response, null, null, null);
        }

        double bestDistance = Double.MAX_VALUE;
        NeighbourResponse bestOriginNeighbour = null;
        NeighbourResponse bestDestinationNeighbour = null;
        RouteInfo bestRouteInfo = null;

        for(NeighbourResponse nOrigin : originNeighbours) {
            for (NeighbourResponse nDestination : destinationNeighbours) {
                RouteInfo ri = graph.route(nOrigin.getPoint().getCode(), nDestination.getPoint().getCode());
                double tempDistance = nOrigin.getDistance() + ri.getDistance() + nDestination.getDistance();
                if (tempDistance < bestDistance) {
                    bestDistance = tempDistance;
                    bestOriginNeighbour = nOrigin;
                    bestDestinationNeighbour = nDestination;
                    bestRouteInfo = ri;
                }
            }
        }
        response.setOriginNeighbour(bestOriginNeighbour.getPoint());
        response.setDestinationNeighbour(bestDestinationNeighbour.getPoint());
        return routeComposition(response, bestRouteInfo, bestOriginNeighbour.getDistance(), bestDestinationNeighbour.getDistance());
    }

    private boolean checkIntersectionBetweenNeighbours(List<NeighbourResponse> originNeighbours, List<NeighbourResponse> destinationNeighbours) {
        for (Point p : originNeighbours.stream().map(NeighbourResponse::getPoint).collect(Collectors.toList()))
            if (destinationNeighbours.stream().map(NeighbourResponse::getPoint).collect(Collectors.toList()).contains(p))
                return true;
        return false;
    }
        /**
         * Remove from the input list all the points with a distance with respect to the first element
         * above the threshold
         * @param listOfNeighbours list of points to be filtered
         * @return A filtered list of points
         */
    private List<NeighbourResponse> neighboursFiltering(List<NeighbourResponse> listOfNeighbours) {
        listOfNeighbours.sort(Comparator.comparingDouble(NeighbourResponse::getDistance));
        NeighbourResponse nearest = listOfNeighbours.get(0);
        HeartDistance calculator = new HeartDistance();
        return listOfNeighbours.stream().filter(p -> calculator.calculate(p.getPoint(), nearest.getPoint()) < NEIGHBOURS_THRESHOLD).collect(Collectors.toList());
    }

    /**
     * If the middlePath flag is false then the Haversine distance is computed otherwise the response
     * is composed
     * @param response OverlayResponse we are working with
     * @param overlayDistance precomputed distance of the overlay graph
     * @return An OverlayResponse with all the information of the route
     * the selected node as part of the OverlayGraph
     */
    public OverlayResponse routeComposition(OverlayResponse response, RouteInfo overlayDistance, Double originDistance, Double destinationDistance) {
        HeartDistance distCalculator = new HeartDistance();

        if(!response.getMiddlePath()) {
            response.computeTimeWithSpeedProfile(distCalculator.
                    calculate(response.getOrigin(),response.getDestination()));
            return response;
        }
        if(response.getInitialPath()) {
            response.setDistance(originDistance);
        }

        response.concat(overlayDistance);

        if(response.getFinalPath()) {
            response.setDistance(destinationDistance);
        }
        return response;
    }

    public void printGraph() {
        System.out.println("Our overlay graph is: ");
        graph.print();
    }


}
