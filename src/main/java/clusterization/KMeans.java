package clusterization;

import location_iq.Point;
import util.Distance;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Class that implements the KMeans algorithm
 */
public class KMeans {
    /**
     * Constructor of the KMeans
     * @param records Set of points to be clustered
     * @param k Number of clusters: is the number of set into which the points will be clustered
     * @param distance An object representing the type of distance to be used to calculate the distance between
     * points and centroids
     * @param maxIter Is the maximum admitted number of iterations of the algorithm after which the algorithm will
     * be blocked
     * @return A ClusterSet object
     */
    public static ClusterSet compute (List<Point> records, int k,
                                      Distance distance, int maxIter) {
        //Initialization of the centroids
        List<Point> centroids = randomCentroids(records, k);
        Map<Point, List<Point>> clusters = new HashMap<>();
        Map<Point, List<Point>> lastState = new HashMap<>();
        boolean shouldTerminate = false;
        for (int i = 0; !shouldTerminate; i++) {

            for (Point p : records) {
                Point centroid = nearestCentroid(p, centroids, distance);
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

    /**
     * Returns a list of initial random Centroids to start the computation.
     * The centroids initially will be random and distributed around the points in order to ensure a better
     * algorithm execution
     * @param records List of input points
     * @param k number of clusters
     * @return List of Centroids
     */
    private static List<Point> randomCentroids(List<Point> records, int k) {
        List<Point> centroids = new ArrayList<>();
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
            centroids.add(new Point(lat, lon));
        }
        return centroids;
    }

    /**
     * Calculate for the given point the nearest Centroid
     * @param point Point to be processed
     * @param centroids List of all the centroids
     * @param distance Type of Distance that must be used to measure the nearest centroids
     * @return One element of the centroids the is the nearest to point
     */
    private static Point nearestCentroid(Point point, List<Point> centroids, Distance distance) {
        double minimumDistance = Double.MAX_VALUE;
        Point nearest = null;

        for (Point centroid : centroids) {
            double currentDistance = distance.calculate(centroid, point);

            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                nearest = centroid;
            }
        }

        return nearest;
    }

    /**
     * Update the clusters hashmap with the centroid and the list of points
     * @param clusters
     * @param point
     * @param centroid
     */
    private static void assignToCluster(Map<Point, List<Point>> clusters, Point point, Point centroid) {
        clusters.compute(centroid, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }

            list.add(point);
            return list;
        });
    }

    /**
     * Calculate the new centroid for each new cluster
     * @param centroid Old centroid
     * @param records List of points into the cluster
     * @return the new centroid
     */
    private static Point average(Point centroid, List<Point> records) {
        if (records == null || records.isEmpty()) {
            return centroid;
        }

        double avgLat = 0.0;
        double avgLon = 0.0;

        for (Point p : records) {
            avgLat += p.getLatitude();
            avgLon += p.getLongitude();
        }

        return new Point(avgLat / records.size(), avgLon / records.size());
    }

    /**
     * Parse the clusters and calculate the new centroid for each cluster calling average
     * @param clusters ClusterSet as map form
     * @return ClusterSet as map form
     */
    private static List<Point> relocateCentroids(Map<Point, List<Point>> clusters) {
        return clusters.entrySet().stream().map(e -> average(e.getKey(), e.getValue())).collect(toList());
    }
}
