package overlay_matrix_graph;

import com.graphhopper.PathWrapper;
import graph_hopper.GraphHopperInstance;
import junit.framework.TestCase;
import input_output.ExcelReader;
import input_output.exceptions.CellTypeException;
import objects.Point;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;

import java.io.IOException;

public class MatrixOverlayGraphManagerTest extends TestCase {

    private static final String PATH = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";

    public void testDifferencesBetweenOverlayRoutesAndGH() {
        //Creation of the two graphs (Overlay and GraphHopper)
        MatrixOverlayGraphManager og = new MatrixOverlayGraphManager();
        og.loadOrCreateGraph();
        GraphHopperInstance gh = new GraphHopperInstance();
        gh.preprocessing();
        //Read a random pair (origin, destination)
        ExcelReader reader = null;
        try {
            reader = new ExcelReader(PATH);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        boolean repeat = false;
        Point origin = null, destination = null;
        do {
            try {
                reader.setSheetWithIndex(0).initializeIterator().randomRow();
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
        double ghTime = System.nanoTime();
        PathWrapper ghResp = gh.routing(origin, destination);
        ghTime = System.nanoTime() - ghTime;
        double ogTime = System.nanoTime();
        OverlayResponse ogResp = null;
        try {
            ogResp = og.route(origin, destination);
        } catch (NodeCodeNotInOverlayGraphException e) {
            System.err.println("impossibile trovare il nodo nell'overlay graph");
            fail();
        }
        ogTime = System.nanoTime() - ogTime;
        System.out.println("\nGRAPH HOPPER RESULT");
        System.out.println("Time: " + ghResp.getTime() + " [ms]");
        System.out.println("Distance: " + ghResp.getDistance() + " [m]");
        System.out.println("Computed in: " + ghTime);
        System.out.println("\nOVERLAY GRAPH RESULT");
        System.out.println("Time: " + ogResp.getTime() + " [ms]");
        System.out.println("Distance: " + ogResp.getDistance() + " [m]");
        System.out.println("Computed in: " + ogTime);
        if(ogResp.getInitialPath())
            System.out.println("First Overlay step: " + ogResp.getOriginNeighbour());
        if(ogResp.getFinalPath())
            System.out.println("Last Overlay step: " + ogResp.getDestinationNeighbour());
        System.out.println("\nCOMPARISON");
        System.out.println("Time error is " + (ogResp.getTime() - ghResp.getTime()) + " [ms] over " + ghResp.getTime() + " [ms]");
        System.out.println("Time error in seconds " + (ogResp.getTime() - ghResp.getTime())/1000 + " [s] over " + ghResp.getTime()/1000 + " [s]");
        System.out.println("Time error in hours " + ((float)((ogResp.getTime() - ghResp.getTime())/1000))/3600 + " [h] over " + ((float)(ghResp.getTime()/1000))/3600 + " [h]");
        System.out.println("Distance error is " + ((int)(ogResp.getDistance() - ghResp.getDistance())) + " [m] over " + ((int)ghResp.getDistance())  + " [m]");
        System.out.println("Distance error in kilometers " + ((float)((int)(ogResp.getDistance() - ghResp.getDistance())))/1000 + " [km] over " + ((float)((int)ghResp.getDistance()))/1000  + " [km]");
        System.out.println("Execution speedUp: " + ((int)(ghTime/ogTime)));
        assertTrue(true);
    }
}