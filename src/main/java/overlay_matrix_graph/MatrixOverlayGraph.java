package overlay_matrix_graph;

import objects.ParamsObject;
import objects.Point;
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

    /**
     * This param says if the first split of a KdTree will be done by a latitude split
     * or a longitude split
     */
    private boolean SPLIT_LATITUDE = true;
    /**
     * This param define the number of neighbours for each NN research
     */
    private int NUMBER_OF_NN = 20;

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
        System.out.println("Graph Dimension: " + graph.size());
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

    public void setParams (ParamsObject po) {
        if(po == null)
            return;
        if(po.isSplitLatitude() != null) this.SPLIT_LATITUDE = po.isSplitLatitude();
        if(po.getNumberNN() != null) this.NUMBER_OF_NN = po.getNumberNN();
    }

    public List<NeighbourResponse> searchNeighbour(Point p) {
        if(useKdTree)
            return kdTreeSupporter.searchNeighbours(p, NUMBER_OF_NN);
        return linearSupporter.searchNeighbours(p, NUMBER_OF_NN);
    }

    public List<NeighbourResponse> searchNeighbourWithAngleHint(Point p, double angle) {
        List<NeighbourResponse> neighbours;
        if (useKdTree)
            neighbours = kdTreeSupporter.searchNeighbours(p, NUMBER_OF_NN, angle);
        else
            neighbours = linearSupporter.searchNeighbours(p, NUMBER_OF_NN, angle);
        if (neighbours.isEmpty()) {
            System.out.println("With the angle hint no neighbours have been founded \n " +
                    "The standard approach have been used");
            if (useKdTree)
                neighbours = kdTreeSupporter.searchNeighbours(p, NUMBER_OF_NN);
            else
                neighbours = linearSupporter.searchNeighbours(p, NUMBER_OF_NN);
        }
        return neighbours;
    }

    /**
     * Compute the route using both origin and destination as point on the overlay graph
     */
    public RouteInfo route(String fromCode, String toCode) {
            return graph.get(fromCode).route(toCode);
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
        } catch (IndexOutOfBoundsException e) {
            throw new NodeNotInOverlayGraphException();
        }
    }


}
