package overlay_matrix_graph;

import location_iq.Point;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.quadTree.QuadTreeNode;
import util.HeartDistance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class MatrixOverlayGraph implements Serializable {
    private HashMap<String, Source> graph;
    private QuadTreeNode quadTreeSupport;

    public MatrixOverlayGraph() {
        graph = new HashMap<>();
    }

    public void put(String code, Source source) {
        graph.put(code,source);
    }

    public void print() {
        graph.forEach((c,p) -> {
            System.out.println(" - Source: " + c);
            p.print();
        });
    }

    public void createSupporters() {
        quadTreeSupport = new QuadTreeNode(
                graph.values().stream().map(Source::getNodeInfo)
                        .collect(Collectors.toList())
        );
    }

    /**
     * If one or both of the points is part of the graph the reasearch will be simplified by the use of
     * the others route function
     * Compute the response as a combination of route: the vicinities of the graph plus
     * @param fromPoint Origin of the path
     * @param toPoint Destination of the path
     * @return Ovarlay response with the data of the route
     * @throws NodeCodeNotInOverlayGraphException reaised if it is impossible to find a point in the
     * overlay graph
     */
    public OverlayResponse route(Point fromPoint, Point toPoint) throws NodeCodeNotInOverlayGraphException {
        List<Point> l = pointPresentIntoGraph(fromPoint);
        if (!l.isEmpty())
            return route(l.get(0).getCode(), toPoint);
        l = pointPresentIntoGraph(toPoint);
        if(!l.isEmpty())
            return route(fromPoint, l.get(0).getCode());
        Point neighbourFrom = quadTreeSupport.searchNeighbour(fromPoint);
        Point neighbourTo = quadTreeSupport.searchNeighbour(toPoint);
        return new OverlayResponse().computeTimeFromHeversineDistance(new HeartDistance().
                calculate(neighbourFrom, fromPoint)).
                concat(route(neighbourFrom.getCode(), neighbourTo.getCode())).
                concat(new OverlayResponse().computeTimeFromHeversineDistance(new HeartDistance().
                        calculate(neighbourTo, toPoint)));
    }

    public OverlayResponse route(String fromCode, Point toPoint) throws NodeCodeNotInOverlayGraphException {
        List<Point> l = pointPresentIntoGraph(toPoint);
        if(!l.isEmpty())
            return route(fromCode, l.get(0).getCode());
        Point neighbour = quadTreeSupport.searchNeighbour(toPoint);
        return route(fromCode, neighbour.getCode()).
                concat(new OverlayResponse().computeTimeFromHeversineDistance(
                        new HeartDistance().calculate(neighbour, toPoint)));
    }

    public OverlayResponse route(Point fromPoint, String toCode) throws NodeCodeNotInOverlayGraphException {
        List<Point> l = pointPresentIntoGraph(fromPoint);
        if(!l.isEmpty())
            return route(l.get(0).getCode(), toCode);
        Point neighbour = quadTreeSupport.searchNeighbour(fromPoint);
        return new OverlayResponse().computeTimeFromHeversineDistance(
                        new HeartDistance().calculate(neighbour, fromPoint)).
                concat(route(neighbour.getCode(), toCode));
    }

    public OverlayResponse route(String fromCode, String toCode) throws NodeCodeNotInOverlayGraphException {
        try {
            return graph.get(fromCode).route(toCode);
        } catch (NullPointerException e) {
            throw new NodeCodeNotInOverlayGraphException("The node " + fromCode +
                    " is not present in the Overlay Graph, impossible to route");
        }
    }

    private List<Point> pointPresentIntoGraph(Point p) {
        return graph.
                values().
                stream().
                map(Source::getNodeInfo).
                filter(s -> s.getLongitude().equals(p.getLongitude()) &&
                        s.getLatitude().equals(p.getLatitude())).
                collect(Collectors.toList());
    }

}
