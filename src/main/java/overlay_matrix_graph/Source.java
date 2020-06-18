package overlay_matrix_graph;

import location_iq.Point;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents each possible source of the overlay graph as a Point with its own data (nodeInfo)
 * plus the HashMap routes, this hashmap contains a series of pair <String, RouteInfo> each one represent
 * the data to route from the source to the relative point represented by the unique code ad attached to
 * the routeInfo object that contains the information of the route(time, distance ..)
 */
public class Source implements Serializable {
    private Point nodeInfo;
    private HashMap<String,RouteInfo> routes;

    public Source() {
        routes = new HashMap<>();
    }

    /**
     * Take a map with the route information, verify that the request is not failed and add this to the routes
     * HashMap
     * @param m A map with the data to route from the sorce to a specific point
     */
    public void addNewPath(Map m) {
        if(!m.get("code").toString().equalsIgnoreCase("Failed request")) {
            routes.put(m.get("code").toString(),
                    new RouteInfo(Double.parseDouble(m.get("time").toString()),
                            Double.parseDouble(m.get("distance").toString())));
        }
    }

    /**
     * Print the graph, used for test
     */
    public void print(){
        routes.forEach((s,r) -> {
            System.out.println("\t - Destination: " + s);
            System.out.println("\t   Time: " + r.getTime() + " - Distance: " + r.getDistance());
        });
    }

    public void setNodeInfo(Point nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    public Point getNodeInfo() {
        return nodeInfo;
    }

    /**
     * Take the data into routeInfo and compute a response from it
     * @param toCode Represents the destination point over the overlay graph, the origin will be the point
     * containing this object
     * @return An overlayResponse containing the precomputed overlay's data
     * @throws NodeCodeNotInOverlayGraphException This exception will be thrown if the code represented by the
     * param <toCode> is not present into the overlay graph
     */
    public OverlayResponse route(String toCode) throws NodeCodeNotInOverlayGraphException {
        try {
            return routes.get(toCode).getResponse();
        } catch (NullPointerException e) {
            throw new NodeCodeNotInOverlayGraphException("The node " + toCode +
                    " is not present in the Overlay Graph, impossible to route");
        }
    }
}
