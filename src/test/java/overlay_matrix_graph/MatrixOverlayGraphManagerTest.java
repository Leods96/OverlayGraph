package overlay_matrix_graph;

import com.graphhopper.PathWrapper;
import graph_hopper.GHTest;
import junit.framework.TestCase;
import location_iq.ExcellReader;
import location_iq.Exceptions.CellTypeException;
import location_iq.Point;
import overlay_matrix_graph.Exceptions.NodeCodeNotInOverlayGraphException;

import java.io.IOException;

public class MatrixOverlayGraphManagerTest extends TestCase {

    private static final String PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";

    public void testDifferencesBetweenOverlayRoutesAndGH() {
        //Creation of the two graphs (Overlay and GraphHopper)
        MatrixOverlayGraphManager og = new MatrixOverlayGraphManager();
        og.loadOrCreateGraph();
        GHTest gh = new GHTest();
        gh.preprocessing();
        //Read a random pair (origin, destination)
        ExcellReader reader = null;
        try {
            reader = new ExcellReader(PATH);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        boolean repeat = false;
        Point origin = null, destination = null;
        do {
            try {
                reader.setSheetWithIndex(1).initializeIterator().randomRow();
                origin = new Point(reader.getID(), reader.getLatitude(), reader.getLongitude());
            } catch (CellTypeException e) {
                repeat = true;
            }
        } while (!repeat);
        repeat = false;
        do {
            try {
                reader.setSheetWithIndex(0).initializeIterator().randomRow();
                destination = new Point(reader.getID(), reader.getLatitude(), reader.getLongitude());
            } catch (CellTypeException e) {
                repeat = true;
            }
        } while (!repeat);
        System.out.println("Origin: " + origin);
        System.out.println("Destination: " + destination);
        //Routing
        PathWrapper ghResp = gh.routing(origin, destination);
        OverlayResponse ogResp = null;
        try {
            ogResp = og.route(origin, destination);
        } catch (NodeCodeNotInOverlayGraphException e) {
            System.err.println("impossibile trovare il nodo nell'overlay graph");
            fail();
        }
        System.out.println("Graph Hopper result");
        System.out.println("Time: " + ghResp.getTime());
        System.out.println("Distance: " + ghResp.getDistance());
        System.out.println("Overlay Graph result");
        System.out.println("Time: " + ogResp.getTime());
        System.out.println("Distance: " + ogResp.getDistance());
        assertTrue(true);
    }
}