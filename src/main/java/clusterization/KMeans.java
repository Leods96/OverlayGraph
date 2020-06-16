package clusterization;

import location_iq.Point;
import util.Distance;
import util.EuclideanDistance;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class KMeans {
    public static ClusterSet compute (List<Point> records, int k,
                                      EuclideanDistance distance, int maxIter) {
        //Initialization of the centroids
        List<Centroid> centroids = randomCentroids(records, k);
        Map<Centroid, List<Point>> clusters = new HashMap<>();
        Map<Centroid, List<Point>> lastState = new HashMap<>();
        boolean shouldTerminate = false;
        for (int i = 0; !shouldTerminate; i++) {

            for (Point p : records) {
                Centroid centroid = nearestCentroid(p, centroids, distance);
                assignToCluster(clusters, p, centroid);
            }

            shouldTerminate = (i == maxIter - 1) || clusters.equals(lastState);
            lastState = clusters;
            if (!shouldTerminate) {
                centroids = relocateCentroids(clusters);
                clusters = new HashMap<>();
            }
        }
        return new ClusterSet(lastState);
    }

    private static List<Centroid> randomCentroids(List<Point> records, int k) {
        List<Centroid> centroids = new ArrayList<>();
        Random random = new Random();
        double maxLat = Double.MIN_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        for(Point p : records) {
            maxLat = p.getLatitude() > maxLat ? p.getLatitude() : maxLat;
            minLat = p.getLatitude() < minLat ? p.getLatitude() : minLat;
            maxLon = p.getLongitude() > maxLon ? p.getLongitude() : maxLon;
            minLon = p.getLongitude() < minLon ? p.getLongitude() : minLon;
        }
        for (int i = 0; i < k; i++) {
            double lat = random.nextDouble() * (maxLat - minLat) + minLat;
            double lon = random.nextDouble() * (maxLon - minLon) + minLon;
            centroids.add(new Centroid(lat, lon));
        }
        return centroids;
    }

    private static Centroid nearestCentroid(Point point, List<Centroid> centroids, Distance distance) {
        double minimumDistance = Double.MAX_VALUE;
        Centroid nearest = null;

        for (Centroid centroid : centroids) {
            double currentDistance = distance.calculate(centroid, point);

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }

        return nearest;
    }

    private static void assignToCluster(Map<Centroid, List<Point>> clusters, Point point, Centroid centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(point);
            return list;
        });
    }

    private static Centroid average(Centroid centroid, List<Point> records) {
        if (records == null || records.isEmpty()) {
            return centroid;
        }

        double avgLat = 0.0;
        double avgLon = 0.0;

        for (Point p : records) {
            avgLat += p.getLatitude();
            avgLon += p.getLongitude();
        }

        return new Centroid(avgLat / records.size(), avgLon / records.size());
    }

    private static List<Centroid> relocateCentroids(Map<Centroid, List<Point>> clusters) {
        return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
    }
}
