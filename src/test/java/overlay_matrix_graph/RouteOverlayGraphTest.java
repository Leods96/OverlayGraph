package overlay_matrix_graph;

import junit.framework.TestCase;
import objects.Point;
import org.junit.Test;
import util.Util;


public class RouteOverlayGraphTest extends TestCase {

    private final String GRAPH_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private final String DUMP_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\ExitsDumps\\";

    @Test
    public void testUniqueRouteWithDataTest() {
        MatrixOverlayGraphManager graphManager = new MatrixOverlayGraphManager();
        graphManager.setGraphPath(GRAPH_FOLDER);
        long time = System.nanoTime();
        graphManager = Util.creationOfOverlayGraph(DUMP_FOLDER, GRAPH_FOLDER, false);
        System.out.println("Elapsed time to load the graph in second: " + (System.nanoTime()-time)/1000000000);


        Point origin = new Point("T791", 39.81267, 16.20158);
        Point destination = new Point("TKR1", 39.59752, 16.51455);

        OverlayResponse response1 = null;
        OverlayResponse response2 = null;
        response1 = graphManager.route(origin, destination);
        response2 = graphManager.route(destination, origin);

        StringBuilder sb = new StringBuilder();
        sb.append("https://www.google.nl/maps/dir");
        sb.append("/").append(response1.getOrigin().getLatitude()).append(",+").append(response1.getOrigin().getLongitude());
        sb.append("/").append(response1.getOriginNeighbour().getLatitude()).append(",+").append(response1.getOriginNeighbour().getLongitude());
        sb.append("/").append(response1.getDestinationNeighbour().getLatitude()).append(",+").append(response1.getDestinationNeighbour().getLongitude());
        sb.append("/").append(response1.getDestination().getLatitude()).append(",+").append(response1.getDestination().getLongitude());

        System.out.println(sb.toString());

        response1.printResponse();

        System.out.println("\n\n\n");
        response2.printResponse();
    }
}
