package util;

import objects.Point;

import static java.lang.Math.*;

public class HeartDistance implements Distance{
    /**
     * mean radius of the earth in meters
     */
    public static final double R = 6371000; // m

    /**
     * mean radius of earth in km
     */
    public static final double R_KM = 6371.0088; //km

    /**
     * Radius of the earth at equator
     */
    public static final double R_EQ = 6378137; // m
    /**
     * Circumference of the earth
     */
    public static final double C = 2 * PI * R;
    public static final double KM_MILE = 1.609344;
    public static final double METERS_PER_DEGREE = C / 360.0;

    /**
     * Calculates distance of (from, to) in meter.
     * http://en.wikipedia.org/wiki/Haversine_formula a = sin²(Δlat/2) +
     * cos(lat1).cos(lat2).sin²(Δlong/2) c = 2.atan2(√a, √(1−a)) d = R.c
     */
    public double calculate(Point from, Point to) {
        double normedDist = calcNormalizedDist(from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
        return R * 2 * asin(sqrt(normedDist));
    }

    /**
     * Returns the specified length in normalized meter.
     */
    public double calcNormalizedDist(double fromLat, double fromLon, double toLat, double toLon) {
        double sinDeltaLat = sin(toRadians(toLat - fromLat) / 2);
        double sinDeltaLon = sin(toRadians(toLon - fromLon) / 2);
        return sinDeltaLat * sinDeltaLat
                + sinDeltaLon * sinDeltaLon * cos(toRadians(fromLat)) * cos(toRadians(toLat));
    }

}
