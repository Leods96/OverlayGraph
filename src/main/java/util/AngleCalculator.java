package util;

import objects.Point;

public class AngleCalculator {

    private static final int RANGE = 80;

    public static double getAngle(Point start, Point end) {
        double lon1 = start.getLongitude();
        double lat1 = start.getLatitude();
        double lon2 = end.getLongitude();
        double lat2 = end.getLatitude();

        double dLon = Math.toRadians(lon2-lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));

        // fix negative degrees
        if(brng<0) {
            brng=360-Math.abs(brng);
        }

        return brng;
    }

    public static boolean isInRange(double startingAngle, double angleToBeTested) {
        return (angleToBeTested < startingAngle) ? startingAngle - angleToBeTested < RANGE : angleToBeTested - startingAngle < RANGE;
    }
}
