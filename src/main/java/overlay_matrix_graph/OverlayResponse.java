package overlay_matrix_graph;

import location_iq.Point;

import java.util.ArrayList;

public class OverlayResponse {
    //Speed profile to compute the time in km/h
    private static final int SPEED = 35;
    private static final double FROM_KMH_TO_MS_CONVERSION = 0.277778;

    private ArrayList<Double> time = new ArrayList<>();
    private ArrayList<Double> distance = new ArrayList<>();
    private Point origin;
    private Point destination;
    private Point originNeighbour;
    private Point destinationNeighbour;
    /**
     * These three boolean represent the composition of the route: each boolean represent respectively the
     * presence of a path composing the total path.
     * initialPath: true means that the origin Point is external wrt the overlayGraph and the first part
     * of the path will be a route from this point to the overlay graph, false means that the origin is
     * part of the graph and the route could be simplified
     * middlePath: true means that the overlayGraph will be used to compose the total route, false means
     * that the overlayGraph will not be used
     * finalPath: works as the initialPath but for the destination
     */
    private boolean initialPath = false;
    private boolean middlePath = true;
    private boolean finalPath = false;

    public OverlayResponse (){}

    public OverlayResponse(Point origin, Point destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public OverlayResponse(double time, double distance) {
        this.distance.add(distance);
        this.time.add(time);
    }

    public void setStartingStep() {
        initialPath = true;
    }

    public void removeOverlayFromResponse() {
        middlePath = false;
    }

    public void setFinalStep() {
        finalPath = true;
    }

    public boolean getFinalPath() {
        return finalPath;
    }

    public boolean getInitialPath() {
        return initialPath;
    }

    public boolean getMiddlePath() {
        return middlePath;
    }

    public void setDestinationCode(String code) {
        this.destination.setCode(code);
    }

    public void setOriginCode(String code) {
        this.origin.setCode(code);
    }

    public void setDestinationNeighbour(Point destinationNeighbour) {
        this.destinationNeighbour = destinationNeighbour;
    }

    public void setOriginNeighbour(Point originNeighbour) {
        this.originNeighbour = originNeighbour;
    }

    public Point getDestinationNeighbour() {
        return destinationNeighbour;
    }

    public Point getOriginNeighbour() {
        return originNeighbour;
    }

    public Point getOrigin() {
        return origin;
    }

    public Point getDestination() {
        return destination;
    }

    /**
     * @return time in millisecond
     */
    public int getTime() {
        return (int) time.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * @return distance in meter
     */
    public double getDistance() {
        return distance.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Set the distance and compute the time with respect to a speed profile
     * @return OverlayResponse object
     */
    public OverlayResponse computeTimeWithSpeedProfile(double distance) {
        this.distance.add(distance);
        //TODO check if this is ok
        //conversion of speed in ms and computation of the time converted in millisecond
        this.time.add((distance/(SPEED * FROM_KMH_TO_MS_CONVERSION)*1000));
        return this;
    }

    /**
     * Join of two response object
     */
    public OverlayResponse concat(RouteInfo ri) {
        this.time.add(ri.getTime());
        this.distance.add(ri.getDistance());
        return this;
    }
}
