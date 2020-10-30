package overlay_matrix_graph;

import objects.Point;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.supporters.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOverlayGraph implements Serializable {
    private final HashMap<String, Source> graph;
    private KdTreeSupporter kdTreeSupporter;
    //private LocalityGraph localitySupporter;
    private LinearSupporter linearSupporter;

    //TODO make these params
    private static final boolean SPLIT_LATITUDE = true;
    private static final int NUMBER_OF_NN = 20;
    private boolean useKdTree;

    public MatrixOverlayGraph() {
        graph = new HashMap<>();
    }

    public void put(String code, Source source) {
        graph.put(code,source);
    }

    /**
     * print the graph, for test cases
     */
    public void print() {
        graph.forEach((c,p) -> {
            System.out.println(" - Source: " + c);
            p.print();
        });
    }

    /**
     * Create the supporter from the list of point into the graph
     */
    public void createSupporters(boolean useKdTree) {
        this.useKdTree = useKdTree;
        if(useKdTree) {
            kdTreeSupporter = new KdTreeSupporter
                    (graph.values().stream().map(Source::getNodeInfo).collect(Collectors.toList()),
                    SPLIT_LATITUDE);
            /*localitySupporter = new LocalityGraph
                    (graph.values().stream().map(Source::getNodeInfo).collect(Collectors.toList()),
                    NUMBER_OF_NN);*/
        } else {
            linearSupporter = new LinearSupporter(graph.values().stream().map(Source::getNodeInfo)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * @return the main supporter
     */
    public Supporter getSupporters() {
        if(useKdTree)
            return this.kdTreeSupporter;
        return this.linearSupporter;
    }

    /**
     * @return the locality supporter
     */
   /* public LocalityGraph getLocalitySupporter() {
        return this.localitySupporter;
    } */

    /**
     * Set the support for the neighbour search
     * @param support
     */
    public void setSupporter(Supporter support) {
        useKdTree = support instanceof KdTreeSupporter;
        if(useKdTree)
            this.kdTreeSupporter = (KdTreeSupporter) support;
        else
            this.linearSupporter = (LinearSupporter) support;
    }

    /*public void setLocalitySupporter(LocalityGraph support) {
        this.localitySupporter = support;
    }*/


    //TODO make these two method only one
    public List<NeighbourResponse> searchNeighbour(Point p) {
        if(useKdTree)
            return kdTreeSupporter.searchNeighbours(p, NUMBER_OF_NN);
        return linearSupporter.searchNeighbours(p, NUMBER_OF_NN);
    }

    public List<NeighbourResponse> searchNeighbourWithAngleHint(Point p, double angle) {
        if(useKdTree)
            return kdTreeSupporter.searchNeighbours(p, NUMBER_OF_NN, angle);
        return linearSupporter.searchNeighbours(p, NUMBER_OF_NN, angle);
    }

    /**
     * Compute the route using both origin and destination as point on the overlay graph
     */
    public RouteInfo route(String fromCode, String toCode) throws NodeCodeNotInOverlayGraphException {
        try {
            return graph.get(fromCode).route(toCode);
        } catch (NullPointerException e) {
            throw new NodeCodeNotInOverlayGraphException("The node " + fromCode +
                    " is not present in the Overlay Graph, impossible to route");
        }
    }

    /**
     * Verify if the point is part of the overlay graph and in case return a list containing the point, otherwise
     * return an empty list
     * @param p point to be searched
     * @return list containing a single point part of the graph or an empty list
     */
    public Point pointPresentIntoGraph(Point p) throws NodeNotInOverlayGraphException {
        try {
            return graph.
                    values().
                    stream().
                    map(Source::getNodeInfo).
                    filter(s -> s.getLongitude().equals(p.getLongitude()) &&
                            s.getLatitude().equals(p.getLatitude())).
                    collect(Collectors.toList()).get(0);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw new NodeNotInOverlayGraphException();
        }
    }

}
