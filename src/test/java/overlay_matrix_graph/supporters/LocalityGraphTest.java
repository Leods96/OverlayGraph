package overlay_matrix_graph.supporters;

import junit.framework.TestCase;
import location_iq.Point;
import util.Util;

import java.util.ArrayList;
import java.util.List;

public class LocalityGraphTest extends TestCase {


    public void testLocalityGraph() {
        try {
            List<Point> points = Util.GenerateRandomPoints(50);
            LocalityGraph localityGraph = new LocalityGraph(points, 4);
            System.out.println("Points dim : " + points.size() + " - Locality dim : " + localityGraph.getDimension());
            if(points.size() != localityGraph.getDimension())
                fail();
            LinearSupporter linear = new LinearSupporter(points);
            points.forEach(p -> {
                ArrayList<Point> linearResult = new ArrayList<>(linear.searchNeighbours(p, 4));
                ArrayList<Point> localityResult = new ArrayList<>(localityGraph.getKNN(p));
                System.out.println("Point: " + p);
                System.out.println("Locality: " + localityResult);
                System.out.println("Linear: " + linearResult);
                linearResult.forEach(p1 -> localityResult.remove(p1));
                if(localityResult.size() > 0)
                    fail();
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}