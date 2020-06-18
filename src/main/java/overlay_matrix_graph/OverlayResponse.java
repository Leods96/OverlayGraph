package overlay_matrix_graph;

import location_iq.Point;

public class OverlayResponse {
    //Speed profile to compute the time in km/h
    private static final int SPEED = 35;
    private static final double FROM_KMH_TO_MS_CONVERSION = 0.277778;

    private double time;
    private double distance;
    private Point origin;
    private Point destination;

    public OverlayResponse (){}

    public OverlayResponse(Point origin, Point destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public OverlayResponse(double time, double distance) {
        this.distance = distance;
        this.time = time;
    }

    public void setDestination(Point destination) {
        this.destination = destination;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    /**
     * @return time in second
     */
    public double getTime() {
        return time;
    }

    /**
     * @return distance in meter
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance and compute the time with respect to a speed profile
     * @return OverlayResponse object
     */
    public OverlayResponse computeTimeFromHaversineDistance(double distance) {
        this.distance = distance;
        //TODO check if this is ok
        this.time = distance/(SPEED * FROM_KMH_TO_MS_CONVERSION);
        return this;
    }

    /**
     * Join of two response object
     */
    public OverlayResponse concat(OverlayResponse or) {
        this.time += or.getTime();
        this.distance += or.getDistance();
        return this;
    }
}
