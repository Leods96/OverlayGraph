package clusterization;

import location_iq.Point;

import java.util.List;
import java.util.Map;

public class ClusterSet {
    private Map<Centroid, List<Point>> clusterSet;

    public ClusterSet(Map<Centroid,List<Point>> m) {
        clusterSet = m;
    }

    public Map<Centroid, List<Point>> getClusterSet() {
        return clusterSet;
    }
}
