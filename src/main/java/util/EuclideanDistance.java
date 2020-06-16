package util;

import clusterization.Centroid;
import location_iq.Point;
import util.Distance;

/**
 * Calculates the distance between two items using the Euclidean formula.
 */
public class EuclideanDistance implements Distance {

    public double calculate(Centroid f1, Point f2) {
        if (f1 == null || f2 == null)
            throw new IllegalArgumentException("Feature can't be null");

        return Math.sqrt(Math.pow(f1.getLatitude() - f2.getLatitude(), 2) +
                Math.pow(f1.getLongitude() - f2.getLongitude(), 2));

    }

    public double calculate(Point f1, Point f2) {
        if (f1 == null || f2 == null)
            throw new IllegalArgumentException("Feature can't be null");

        return Math.sqrt(Math.pow(f1.getLatitude() - f2.getLatitude(), 2) +
                Math.pow(f1.getLongitude() - f2.getLongitude(), 2));

    }
}
