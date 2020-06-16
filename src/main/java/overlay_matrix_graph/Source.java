package overlay_matrix_graph;

import location_iq.Point;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Source implements Serializable {
    private Point nodeInfo;
    private HashMap<String,RouteInfo> routes;

    public Source() {
        routes = new HashMap<>();
    }

    public void addNewPath(Map m) {
        if(!m.get("code").toString().equalsIgnoreCase("Failed request")) {
            routes.put(m.get("code").toString(),
                    new RouteInfo(Double.parseDouble(m.get("time").toString()),
                            Double.parseDouble(m.get("distance").toString())));
        }
    }

    public void print(){
        routes.forEach((s,r) -> {
            System.out.println("\t - Destination: " + s);
            System.out.println("\t   Time: " + r.getTime() + " - Distance: " + r.getDistance());
        });
    }

    public Point getNodeInfo() {
        return nodeInfo;
    }

    public OverlayResponse route(String toCode) throws NodeCodeNotInOverlayGraphException {
        try {
            return routes.get(toCode).getResponse();
        } catch (NullPointerException e) {
            throw new NodeCodeNotInOverlayGraphException("The node " + toCode +
                    " is not present in the Overlay Graph, impossible to route");
        }
    }
}
