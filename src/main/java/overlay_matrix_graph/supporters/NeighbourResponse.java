package overlay_matrix_graph.supporters;

import location_iq.Point;

public class NeighbourResponse {

    private Point point;
    private double distance;

    public NeighbourResponse(Point point, double distance) {
        this.point = point;
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
