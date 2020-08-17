package overlay_matrix_graph.supporters;

import location_iq.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalityGraph implements Serializable {
    /**
     * Locality graph, hash that connect each node with his K nearest neighbours
     */
    private final HashMap<String, ArrayList<Point>> graph;
    /**
     * Number of computed nearest neighbours for each nodes
     */
    private final int k;

    /**
     * Create the LocalityGraph based on a given set of point and for each one of these point compute the
     * K nearest neighbours
     * @param points input set of point
     * @param numberOfNeighbours define the number of nearest neighbours founded for each point
     */
    public LocalityGraph(List<Point> points, int numberOfNeighbours) {
        this.k = numberOfNeighbours;
        this.graph = new HashMap<>();
        compute(points);
    }

    /**
     * compute the locality graph based on the linear researcher
     * @param points set of input point
     */
    private void compute(List<Point> points) {
        LinearSupporter nnResearcher = new LinearSupporter(points);
        points.forEach( p ->
            graph.put(p.getCode(), new ArrayList<>(nnResearcher.searchNeighbours(p, k)))
        );
    }

    /**
     * use the graph in order to find the K nearest neighbours
     * @param p point to be searched (must be in the graph)
     * @return The set of K nearest neighbours or null if the point is not presente into the graph
     */
    public List<Point> getKNN(Point p) {
        if(graph.containsKey(p.getCode()))
            return graph.get(p.getCode());
        return null;
    }

    public int getDimension() {
        return graph.size();
    }

}
