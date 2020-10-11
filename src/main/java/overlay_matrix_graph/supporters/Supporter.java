package overlay_matrix_graph.supporters;

import objects.Point;

import java.util.List;

public interface Supporter {

    public Point searchNeighbour(Point point);

    public List<NeighbourResponse> searchNeighbours(Point point, int size);
}
