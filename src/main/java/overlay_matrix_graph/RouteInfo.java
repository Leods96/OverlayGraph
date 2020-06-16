package overlay_matrix_graph;

import java.io.Serializable;

public class RouteInfo implements Serializable {
    private double distance;
    private double time;

    public RouteInfo (double time, double distance) {
        this.distance = distance;
        this.time = time;
    }

    public double getDistance() {
        return distance;
    }

    public double getTime() {
        return time;
    }

    public OverlayResponse getResponse() {
        return new OverlayResponse(time, distance);
    }

}
