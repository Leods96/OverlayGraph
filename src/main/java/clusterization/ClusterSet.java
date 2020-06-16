package clusterization;

import location_iq.Point;

import java.util.List;
import java.util.Map;

/**
 * Represents a set of clusters: the result of the KMeans algorithm, is a map that use the Centroid object
 * as index each one with all the relative points
 */
public class ClusterSet {
    private Map<Point, List<Point>> clusterSet;

    /**
     * Create a ClusterSet given an already formed Map
     * @param m
     */
    public ClusterSet(Map<Point,List<Point>> m) {
        clusterSet = m;
    }

    public Map<Point, List<Point>> getClusterSet() {
        return clusterSet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("#Clusters: " + clusterSet.keySet().size());
        clusterSet.forEach((c,p) -> {
            sb.append("\nCentroid: " + c);
            sb.append("\nPoints: " + p);
        });
        return sb.toString();
    }
}
