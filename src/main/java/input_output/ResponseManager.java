package input_output;

import com.graphhopper.PathWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Object used to filter the GH response
 */
public class ResponseManager {
    /**
     * Extracts from the GH response only the Time and the Distance of the path
     * @param object pathWrapper object that contains the GH response
     * @return map with the filtered data
     */
    public Map<String, Double> filterResponse(PathWrapper object) {
        //TODO check the type of time
        Map<String, Double> response = new HashMap<>();
        response.put("time", (double)object.getTime());
        response.put("distance", object.getDistance());
        /*JSONArray routes = new JSONArray();
        JSONObject point;
        for(Instruction i : object.getInstructions()) {
            point = new JSONObject();
            point.put("name",i.getName());
            point.put("lat",i.getPoints().getLatitude(0));
            point.put("lon",i.getPoints().getLongitude(0));
            routes.put(point);
        }
        response.put("routes",routes); */
        return response;
    }

}
