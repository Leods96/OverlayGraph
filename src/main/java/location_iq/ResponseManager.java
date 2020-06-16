package location_iq;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseManager {
    private boolean wayPoints = true;
    private boolean code = true;
    private boolean geometry = true;
    private boolean summary = true;
    private boolean routes = true;
    private boolean completeRoutes = true;

    public JSONObject filterResponse(JSONObject object) {
        if(!wayPoints)
            object.remove("waypoints");
        if(!code)
            object.remove("code");
        if(!routes)
            object.remove("routes");
        else if(!completeRoutes) {
            JSONArray steps;
            JSONArray newSteps = new JSONArray();
            String geom = null, sum = null;
            if(geometry)
                geom = object.getJSONArray("routes").getJSONObject(0).get("geometry").toString();
            if(summary)
                sum = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).get("summary").toString();
            steps = object.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
            for(Object o : steps){
                newSteps.put(((JSONObject) o).get("maneuver"));
            }
            object.put("routes", newSteps);
            if(geometry)
                object.put("geometry", geom);
            if(summary)
                object.put("summary", sum);
        }
        return object;
    }

    public JSONObject filterResponse(PathWrapper object) {
        JSONObject response = new JSONObject();
        response.put("time", object.getTime());
        response.put("distance", object.getDistance());
        JSONArray routes = new JSONArray();
        JSONObject point;
        for(Instruction i : object.getInstructions()) {
            point = new JSONObject();
            point.put("name",i.getName());
            point.put("lat",i.getPoints().getLatitude(0));
            point.put("lon",i.getPoints().getLongitude(0));
            routes.put(point);
        }
        response.put("routes",routes);
        return response;
    }

    public ResponseManager setWayPoints(boolean wayPoints){
        this.wayPoints = wayPoints;
        return this;
    }

    public ResponseManager setCode(boolean code) {
        this.code = code;
        return this;
    }

    public ResponseManager setRoutes(boolean routes) {
        this.routes = routes;
        return this;
    }

    public ResponseManager setCompleteRoutes(boolean completeRoutes) {
        this.completeRoutes = completeRoutes;
        return this;
    }

    public ResponseManager setGeometry(boolean geometry) {
        this.geometry = geometry;
        return this;
    }

    public ResponseManager setSummary(boolean summary) {
        this.summary = summary;
        return this;
    }

    public void printBalance(JSONObject object) {
        System.out.println("Balance: ");
        for(String key : object.keySet())
            System.out.println("\t" + key + ":" + object.get(key));
    }

    public ResponseManager setForCSV(){
        return this.setCode(false).setCompleteRoutes(false).setGeometry(false).setSummary(true).setWayPoints(false).setRoutes(true);
    }
}
