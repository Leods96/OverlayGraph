package overlay_matrix_graph.supporters;

import location_iq.Point;

import java.util.*;
import java.util.stream.Collectors;

public class CoordinateLocalitySupporter {

    private HashMap<Integer, ArrayList<Point>> latitudeHashMap;
    private HashMap<Integer, ArrayList<Point>> longitudeHashMap;
    private final Integer hashMapDimension;
    private final Double minLat;
    private final Double maxLat;
    private final Double minLon;
    private final Double maxLon;
    private final Double latitudeVariation;
    private final Double longitudeVariation;
    private static final int MIN_NUMBER_NEIGHBOURS = 4;
    private static final int MAX_NUMBER_OF_STEPS = 1;


    public CoordinateLocalitySupporter(List<Point> points, int hashMapDimension) {
        this.hashMapDimension = hashMapDimension;
        double tempMinLat = Double.MAX_VALUE, tempMaxLat = Double.MIN_VALUE;
        double tempMinLon = Double.MAX_VALUE, tempMaxLon = Double.MIN_VALUE;
        for(Point p : points) {
            if(p.getLatitude() > tempMaxLat)
                tempMaxLat = p.getLatitude();
            if(p.getLatitude() < tempMinLat)
                tempMinLat = p.getLatitude();
            if(p.getLongitude() > tempMaxLon)
                tempMaxLon = p.getLongitude();
            if(p.getLongitude() < tempMinLon)
                tempMinLon = p.getLongitude();
        }
        this.minLat = tempMinLat;
        this.maxLat = tempMaxLat;
        this.latitudeVariation = (maxLat - minLat) / hashMapDimension;
        this.minLon = tempMinLon;
        this.maxLon = tempMaxLon;
        this.longitudeVariation = (maxLon - minLon) / hashMapDimension;
        computeHashMaps(points);
    }

    private void computeHashMaps(List<Point> points) {
        latitudeHashMap = new HashMap<>();
        longitudeHashMap = new HashMap<>();
        for(int bucketIndex = 0; bucketIndex < hashMapDimension; bucketIndex++) {
            longitudeHashMap.put(bucketIndex, new ArrayList<>());
            latitudeHashMap.put(bucketIndex, new ArrayList<>());
        }
        points.forEach(p -> {
            int bucketIndex = computeLatitudeCode(p);
            latitudeHashMap.get(bucketIndex).add(p);
            bucketIndex = computeLongitudeCode(p);
            try {
                longitudeHashMap.get(bucketIndex).add(p);
            } catch (NullPointerException e) {
                System.out.println("Error - Point: " + p);
                System.out.println("Error - bucket: " + bucketIndex);
            }
        });
    }

    private int computeLatitudeCode(Point point) {
        if(point.getLatitude() <= minLat)
            return 0;
        if(point.getLatitude() >= maxLat)
            return hashMapDimension - 1;
        return (int)((point.getLatitude() - minLat) / latitudeVariation);
    }

    private int computeLongitudeCode(Point point) {
        if(point.getLongitude() <= minLon)
            return 0;
        if(point.getLongitude() >= maxLon)
            return hashMapDimension - 1;
        return (int)((point.getLongitude() - minLon) / longitudeVariation);
    }

    public List<Point> searchNeighbours(Point point) {
        ArrayList<Point> latitudeNeighbour = latitudeHashMap.get(computeLatitudeCode(point));
        ArrayList<Point> longitudeNeighbour = longitudeHashMap.get(computeLongitudeCode(point));
        ArrayList<Point> points = new ArrayList<>(getPointsIntersect(latitudeNeighbour, longitudeNeighbour));
        if(points.size() >= MIN_NUMBER_NEIGHBOURS)
            return points;
        return searchNeighbours(point, 1, latitudeNeighbour, longitudeNeighbour);
    }

    public List<Point> searchNeighbours(Point point, int step, List<Point> latitudeNeighbour, List<Point> longitudeNeighbour) {
        int latBucketIndex = computeLatitudeCode(point);
        int lonBucketIndex = computeLongitudeCode(point);

        if(latBucketIndex - step >= 0)
            latitudeNeighbour.addAll(latitudeHashMap.get(latBucketIndex - step));
        if(latBucketIndex + step <= hashMapDimension - 1)
            latitudeNeighbour.addAll(latitudeHashMap.get(latBucketIndex + step));

        if(lonBucketIndex - step >= 0)
            longitudeNeighbour.addAll(longitudeHashMap.get(lonBucketIndex - step));
        if(lonBucketIndex + step <= hashMapDimension - 1)
            longitudeNeighbour.addAll(longitudeHashMap.get(lonBucketIndex + step));

        ArrayList<Point> points = new ArrayList<>(getPointsIntersect(latitudeNeighbour, longitudeNeighbour));
        if(points.size() >= MIN_NUMBER_NEIGHBOURS || step >= MAX_NUMBER_OF_STEPS)
            return points;
        return searchNeighbours(point, step + 1, latitudeNeighbour, longitudeNeighbour);
    }

    private List<Point> getPointsIntersect(List<Point> a, List<Point> b) {
        return a.stream().filter(b::contains).collect(Collectors.toList());
    }

}
