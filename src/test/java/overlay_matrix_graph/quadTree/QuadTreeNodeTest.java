package overlay_matrix_graph.quadTree;

import junit.framework.TestCase;
import location_iq.Point;
import org.junit.Before;
import org.junit.Test;
import util.EuclideanDistance;
import util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuadTreeNodeTest extends TestCase {
    private static final int NUMBER_OF_POINTS = 10000;
    private static final List<Point> points = new ArrayList<>();
    private QuadTreeNode tree;

    @Before
    public void treeCreation() {
        points.addAll(Util.GenerateRandomPoints(NUMBER_OF_POINTS));
        tree = new QuadTreeNode(points);
    }

    @Test
    public void testTreeSearchCorrectnessAndTime() {
        treeCreation();
        tree.printTree();
        Point pointToSearch = Util.GenerateRandomPoints(1).get(0);
        System.out.println("\nPoint to be searched: " + pointToSearch);
        double standardTime = System.nanoTime();
        Point standardSerachPoint = standardSearch(pointToSearch, points);
        standardTime = System.nanoTime() - standardTime;
        double treeTime = System.nanoTime();
        Point treeSearchPoint = treeSearch(pointToSearch, tree);
        treeTime = System.nanoTime() - treeTime;
        assertEquals(treeSearchPoint, standardSerachPoint);
        assertTrue(treeTime < standardTime);
        System.out.println("Speedup of: " + new Double(treeTime/standardTime * 100).shortValue() + "% over " + NUMBER_OF_POINTS + " points");
    }

    public Point standardSearch(Point p, List<Point> points) {
        double minDist = Double.MAX_VALUE;
        Point toBeRet = null;
        for(Point ps : points) {
            double temp = new EuclideanDistance().calculate(ps, p);
            if (temp < minDist) {
                toBeRet = ps;
                minDist = temp;
            }
        }
        return toBeRet;
    }

    public Point treeSearch(Point p, QuadTreeNode tree){
        return tree.searchNeighbour(p);
    }

    public void mapTest() {
        HashMap<Point, List<Point>> h = new HashMap();
    }

}