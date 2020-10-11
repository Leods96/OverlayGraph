package overlay_matrix_graph.supporters;

import objects.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HammingDistanceSupporter {
    /**
     * MAX_ALLOWED_LEVELS represent the maximum number of level allowed into the tree, this means that the
     * most precise comparison will be done based on the 6th decimal digit of the coordinates
     */
    private static final int MAX_ALLOWED_LEVELS = 6;
    private static int maxNumOfLevels;
    private HashMap<String, HammingDistanceSupporter> nextLevels = null;
    private ArrayList<Point> bucket = null;
    private final int level;
    private static final double LOCALITY_THRESHOLD = 0.25;

    /**
     * Builder of the root, initialize the first bucket as the list of point given and forward the recursive
     * creation of the tree
     * @param records List of nodes on which will be performed the tree construction
     * @param levels Define the maximum number of level for this tree and the relative precision
     */
    public HammingDistanceSupporter (List<Point> records, int levels) {
        maxNumOfLevels = Math.max(levels, 0);
        System.out.println("maxNumOfLevels = " + maxNumOfLevels);
        this.level = 0;
        this.bucket = new ArrayList<>(records);
        if(this.level + 1 <= maxNumOfLevels && this.level + 1 <= MAX_ALLOWED_LEVELS)
            this.computeNextBuckets();
    }

    public HammingDistanceSupporter(int level) {
        this.level = level;
    }

    public HammingDistanceSupporter addNewPoint(Point p) {
        if(bucket == null)
            bucket = new ArrayList<>();
        this.bucket.add(p);
        return this; //Useful to use the compute function
    }

    public void computeNextBuckets() {
        nextLevels = new HashMap<>();
        bucket.forEach( p -> {
            String code = getLevelCode(p);
            nextLevels.computeIfAbsent(code, v -> new HammingDistanceSupporter(this.level + 1));
            nextLevels.computeIfPresent(code, (k, v) -> v.addNewPoint(p));
        });
        if(this.level + 1 <= maxNumOfLevels && this.level + 1 <= MAX_ALLOWED_LEVELS)
            nextLevels.values().forEach(HammingDistanceSupporter::computeNextBuckets);
    }

    private String getLevelCode(Point point) {
        if(this.level == 0)
            return point.getLatitude().intValue() + "-" + point.getLongitude().intValue();
        char[] latVal = String.valueOf(point.getLatitude() - point.getLatitude().intValue()).toCharArray();
        char[] lonVal = String.valueOf(point.getLongitude() - point.getLongitude().intValue()).toCharArray();
        return latVal[this.level + 1] + "-" + lonVal[this.level + 1];
    }

    public List<Point> searchNearestBucket(Point point) {
        if(this.level == maxNumOfLevels || this.level == MAX_ALLOWED_LEVELS)
            return bucket;
        Double firstLatitude = point.getLatitude();
        Double secondLatitude = null;
        Double firstLongitude = point.getLongitude();
        Double secondLongitude = null;
        if(firstLatitude - LOCALITY_THRESHOLD < firstLatitude.intValue())
            secondLatitude = firstLatitude - LOCALITY_THRESHOLD;
        else
            secondLatitude = firstLatitude + LOCALITY_THRESHOLD;
        if(firstLongitude - LOCALITY_THRESHOLD < firstLongitude.intValue())
            secondLongitude = firstLongitude - LOCALITY_THRESHOLD;
        else
            secondLongitude = firstLongitude + LOCALITY_THRESHOLD;


        String code = getLevelCode(point);
        if(nextLevels != null && nextLevels.containsKey(code))
            return nextLevels.get(code).searchNearestBucket(point);
        return bucket;
    }
}
