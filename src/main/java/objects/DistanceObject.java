package objects;

public class DistanceObject {
    private String origin;
    private String destination;
    private double distance;

    public DistanceObject(String origin, String destination, double distance) {
        this.destination = destination;
        this.origin = origin;
        this.distance = distance;
    }

    public DistanceObject(String origin, double distance) {
        this.distance = distance;
        this.origin = origin;
        this.destination = null;
    }

    public double getDistance() {
        return distance;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}
