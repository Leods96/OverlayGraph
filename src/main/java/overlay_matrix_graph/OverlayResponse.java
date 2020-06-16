package overlay_matrix_graph;

public class OverlayResponse {
    //Speed profile to compute the time in km/h
    private static final int SPEED = 35;
    private static final double FROM_KMH_TO_MS_CONVERSION = 0.277778;

    private double time;
    private double distance;


    public OverlayResponse() {

    }

    public OverlayResponse(double time, double distance) {
        this.distance = distance;
        this.time = time;
    }

    public double getTime() {
        return time;
    }

    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance and compute the time with respect to a speed profile
     * @return OverlayResponse object
     */
    public OverlayResponse computeTimeFromHeversineDistance(double distance) {
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
