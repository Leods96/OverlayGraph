package overlay_matrix_graph.supporters;

import location_iq.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KdTreeSupporter implements Serializable, Supporter{

    private final KdNode tree;
    private final boolean splitOnLatitude;

    public KdTreeSupporter(List<Point> points, boolean splitOnLatitude) {
        tree = new KdNode(points, splitOnLatitude);
        this.splitOnLatitude = splitOnLatitude;
    }

    public Point searchNeighbour(Point point) {
        return tree.searchNeighbour(point, tree.getNode(), splitOnLatitude);
    }

    public List<Point> searchNeighbours(Point point, int size) {
        return tree.searchNeighbours(point, new ArrayList<>(), new ArrayList<>(), splitOnLatitude, size);
    }

    public List<Point> searchNeighbours(Point point, int size, double angle) {
        return tree.searchNeighbours(point, new ArrayList<>(), new ArrayList<>(), splitOnLatitude, size, angle);
    }
}
