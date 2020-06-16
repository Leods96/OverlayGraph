package graph_hopper;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;

public class GH_Test {

    private static final String osmFile = "C:\\Users\\leo\\Desktop\\Stage\\OSM\\italy.osm.pbf";
    private static final String workDir = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Graph";
    private static final String vehicle = "car";
    private GraphHopper graphHopper;

    public void preprocessing() {
        graphHopper = new GraphHopper().setGraphHopperLocation(workDir) // "gh-car"
                .setEncodingManager(new EncodingManager(vehicle)) // "car"
                .setOSMFile(osmFile) // "germany-lastest.osm.pbf"
                .forServer();
        System.out.println("Creation or Load of the graph..");
        graphHopper.importOrLoad();
    }

    public PathWrapper routing(double[] origin, double[] destination){
        GHRequest request = new GHRequest(origin[0], origin[1], destination[0], destination[1]);

        request.setWeighting("fastest");
        request.setVehicle(vehicle); // "car"
        GHResponse route = graphHopper.route(request);
        return route.getBest();
    }

}
