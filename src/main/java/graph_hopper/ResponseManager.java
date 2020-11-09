package graph_hopper;

import com.graphhopper.PathWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Object used to filter the GH response
 */
public class ResponseManager {

    private double distance;
    private long time;
    private String message;

    /**
     * Object used to exchange the GH response's data with the dump manager,
     * Extracts from the GH response only the Time and the Distance of the path
     * @param object pathWrapper object that contains the GH response
     */
    public ResponseManager (PathWrapper object) {
        this.distance = object.getDistance();
        this.time = object.getTime();
    }

    public ResponseManager (String message) {
        this.message = message;
    }

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
