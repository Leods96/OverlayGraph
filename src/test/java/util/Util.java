package util;

import location_iq.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
    public static List<Point> GenerateRandomPoints(int dimension) {
        List<Point> points = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < dimension; i++)
            points.add(new Point(random.nextDouble()*100, random.nextDouble()*100));
        return points;
    }
}
