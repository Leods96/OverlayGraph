package overlay_matrix_graph;

import location_iq.Point;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;
import overlay_matrix_graph.exceptions.NodeNotInOverlayGraphException;
import overlay_matrix_graph.quadTree.QuadTreeNode;

import java.io.Serializable;
import java.util.HashMap;
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
     * Create the quadtree from the list of point into the graph
     */
    public void createSupporters() {
        quadTreeSupport = new QuadTreeNode(
                graph.values().stream().map(Source::getNodeInfo)
                        .collect(Collectors.toList())
        );
    }

    /**
     * @return the quadTree
     */
    public QuadTreeNode getSupporters() {
        return this.quadTreeSupport;
    }

    /**
     * Set the support for the neighbour search
     * @param quadTreeSupport
     */
    public void setQuadTreeSupport(QuadTreeNode quadTreeSupport) {
        this.quadTreeSupport = quadTreeSupport;
    }

    public Point searchNeighbour(Point p) {
        return quadTreeSupport.searchNeighbour(p);
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
