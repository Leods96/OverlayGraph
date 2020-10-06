package overlay_matrix_graph.supporters;

import location_iq.Point;
import util.AngleCalculator;
import util.HeartDistance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LinearSupporter implements Serializable, Supporter {
    private final ArrayList<Point> points;
    private ArrayList<NeighbourResponse> bests;

    public LinearSupporter(List<Point> records) {
        this.points = new ArrayList<>(records);
    }

    public List<NeighbourResponse> searchNeighbours(Point point, int size) {
        HeartDistance calculator = new HeartDistance();
        bests = new ArrayList<>();
        points.forEach( p -> {
                double tempDist = calculator.calculate(p, point);
                if (bests.size() < size) {
                    bests.add(new NeighbourResponse(p, tempDist));
                } else {
                    NeighbourResponse worstDistance = takeBigger(bests);
                    if (tempDist < worstDistance.getDistance()) {
                        bests.remove(worstDistance);
                        bests.add(new NeighbourResponse(p, tempDist));
                    }
                }
        });
        return bests;
    }

    public List<NeighbourResponse> searchNeighbours(Point point, int size, double angle) {
        HeartDistance calculator = new HeartDistance();
        bests = new ArrayList<>();
        points.forEach( p -> {
            if(AngleCalculator.isInRange(angle, AngleCalculator.getAngle(point, p))) {
                double tempDist = calculator.calculate(p, point);
                if (bests.size() < size) {
                    bests.add(new NeighbourResponse(p, tempDist));
                } else {
                    NeighbourResponse worstDistance = takeBigger(bests);
                    if (tempDist < worstDistance.getDistance()) {
                        bests.remove(worstDistance);
                        bests.add(new NeighbourResponse(p, tempDist));
                    }
                }
            }
        });
        return bests;
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


    private NeighbourResponse takeBigger(List<NeighbourResponse> list) {
        NeighbourResponse bigger = list.get(0);
        for(int i = 1; i < list.size(); i++)
            if(list.get(i).getDistance() > bigger.getDistance())
                bigger = list.get(i);
        return bigger;
    }
}
