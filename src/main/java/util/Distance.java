package util;

import clusterization.Centroid;
import location_iq.Point;

public interface Distance {

    public double calculate(Point f1, Point f2);
}
