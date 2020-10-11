package clusterization;

import junit.framework.TestCase;
import objects.Point;
import util.Distance;
import util.EuclideanDistance;
import util.Util;

import java.util.ArrayList;
import java.util.List;

public class KMeansTest extends TestCase {
    private static final int NUMBER_OF_POINTS = 20;
    private static final int K = 4;
    private static final Distance DISTANCE = new EuclideanDistance();

    public void testAllPointsNearToAssignedCluster() {
        ClusterSet clusters = KMeans.compute(Util.GenerateRandomPoints(NUMBER_OF_POINTS), K, DISTANCE, 20);
        assertEquals(K, clusters.getClusterSet().keySet().size());
        System.out.println(clusters);
        List<Point> centroids = new ArrayList<>(clusters.getClusterSet().keySet());
        List<Point> points = new ArrayList<>();
        clusters.getClusterSet().forEach((c,p) -> points.addAll(clusters.getClusterSet().get(c)) );
        for(Point c1 : centroids)
            for(Point p : clusters.getClusterSet().get(c1))
                for (Point c2 : centroids)
                    if(!c2.equals(c1))
                            assertFalse(new EuclideanDistance().calculate(c2,p) <
                                    new EuclideanDistance().calculate(c1,p));
    }
}