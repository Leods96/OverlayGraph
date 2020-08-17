package overlay_matrix_graph;

import location_iq.Point;

import java.util.ArrayList;
import java.util.List;

public class OverlayResponse {
    //Speed profile to compute the time in km/h
    //TODO work on speed profile
    private static final int SPEED = 35;
    private static final double FROM_KMH_TO_MS_CONVERSION = 0.277778;

    private ArrayList<Double> time = new ArrayList<>();
    private final ArrayList<Double> distance = new ArrayList<>();
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

    public List<Double> getDistanceList() {
        return distance;
    }

    public List<Double> getTimeList() {
        return time;
    }

    //TODO check the speed profile use
    /**
     * @return time in millisecond
     */
    public long getTime() {
        if(time.isEmpty())
            //l'if check se time è riempito, ora pero' viene sempre riempito per settare la distanza-> andrebbe cambiato
            distance.forEach(d -> this.time.add((d / (SPEED * FROM_KMH_TO_MS_CONVERSION) * 1000)));
        return (long) time.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * @return distance in meter
     */
    public double getDistance() {
        return distance.stream().mapToDouble(Double::doubleValue).sum();
    }

    //TODO this could have no sense, maybe is better to compute te time only if getTime is called
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
     * Join of RouteInfo of the overla graph with the response
     */
    public OverlayResponse concat(RouteInfo ri) {
        this.time.add(ri.getTime());
        this.distance.add(ri.getDistance());
        return this;
    }

    public void printResponse() {
        System.out.println("Origin node: " + origin);
        System.out.println("Destination node: " + destination);
        System.out.println("OriginNeighbour node: " + originNeighbour);
        System.out.println("DestinationNeighbour node: " + destinationNeighbour);

        if(initialPath)
            System.out.println("Initial path = From origin to " + originNeighbour + " : " + distance.get(0));
        if(middlePath)
            System.out.println("Middle path = From " + originNeighbour + " to " + destinationNeighbour + " : " + distance.get(1));
        if(finalPath)
            System.out.println("Final path = From " + destinationNeighbour + " to Destination" + " : " + distance.get(2));
    }
}
