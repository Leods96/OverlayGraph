package overlay_matrix_graph.supporters;

import location_iq.Point;
import util.AngleCalculator;
import util.HeartDistance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearSupporter implements Serializable, Supporter {
    private ArrayList<Point> points;
    private ArrayList<Point> selected;
    private ArrayList<Double> distance;

    public LinearSupporter(List<Point> records) {
        this.points = new ArrayList<>(records);
    }

    public List<Point> searchNeighbours(Point point, int size) {
        HeartDistance calculator = new HeartDistance();
        selected = new ArrayList<>();
        distance = new ArrayList<>();
        points.forEach( p -> {
            double tempDist = calculator.calculate(p, point);
            if(selected.size() < size) {
                selected.add(p);
                distance.add(tempDist);
            } else {
                double worstDistance = takeBigger(distance);
                if(tempDist < worstDistance) {
                    int index = distance.indexOf(worstDistance);
                    distance.remove(index);
                    selected.remove(index);
                    selected.add(p);
                    distance.add(tempDist);
                }
            }
        });
        return selected;
    }

    public List<Point> searchNeighbours(Point point, int size, double angle) {
        HeartDistance calculator = new HeartDistance();
        selected = new ArrayList<>();
        distance = new ArrayList<>();
        points.forEach( p -> {
            if(AngleCalculator.isInRange(angle, AngleCalculator.getAngle(point, p))) {
                double tempDist = calculator.calculate(p, point);
                if (selected.size() < size) {
                    selected.add(p);
                    distance.add(tempDist);
                } else {
                    double worstDistance = takeBigger(distance);
                    if (tempDist < worstDistance) {
                        int index = distance.indexOf(worstDistance);
                        distance.remove(index);
                        selected.remove(index);
                        selected.add(p);
                        distance.add(tempDist);
                    }
                }
            }
        });
        return selected;
    }


    public Point searchNeighbour(Point point) {
        HeartDistance calculator = new HeartDistance();
        Point best = points.get(0);
        for(Point p : points){
            if(calculator.calculate(point, p) < calculator.calculate(point, best))
                best = p;
        }
        return best;
    }


    private double takeBigger(List<Double> dist) {
        double bigger = Double.MIN_VALUE;
        for(Double d : dist)
            if(d > bigger)
                bigger = d;
        return bigger;
    }
}
