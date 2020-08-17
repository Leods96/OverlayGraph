package overlay_matrix_graph.supporters;

import location_iq.Point;
import util.AngleCalculator;
import util.HeartDistance;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

public class KdNode implements Serializable {

    private final Point node;
    private final KdNode leftChild;
    private final KdNode rightChild;

    /*public KdNode(List<Point> points, boolean splitOnLatitude) {
        if(points.size() == 1) {
            node = points.get(0);
            System.out.println("Base case  of dimension 1 for node :" + node +"\n");
            leftChild = null;
            rightChild = null;
        } else {
            if (splitOnLatitude)
                points.sort(Comparator.comparingDouble(Point::getLatitude));
            else
                points.sort(Comparator.comparingDouble(Point::getLongitude));
            System.out.println(splitOnLatitude + " - Sorted list: " + points);
            if (points.size() == 2) {
                node = points.get(1);
                System.out.println("Base case of dimension 2 for node :" + node +"\n");
                leftChild = new KdNode(points.subList(0, 1), !splitOnLatitude);
                rightChild = null;
            } else {
                int split = points.size() / 2;
                node = points.get(split);
                System.out.println("Central case for node :" + node +"\n");
                leftChild = new KdNode(points.subList(0, split), !splitOnLatitude);
                rightChild = new KdNode(points.subList(split + 1, points.size()), !splitOnLatitude);
            }
        }
    }*/

    public KdNode(List<Point> points, boolean splitOnLatitude) {
        if(points.isEmpty()) {
            leftChild = null;
            rightChild = null;
            node = null;
        } else {
            if (splitOnLatitude)
                points.sort(Comparator.comparingDouble(Point::getLatitude));
            else
                points.sort(Comparator.comparingDouble(Point::getLongitude));
            int split = points.size() / 2;
            node = points.get(split);
            leftChild = new KdNode(points.subList(0, split), !splitOnLatitude);
            rightChild = new KdNode(points.subList(split + 1, points.size()), !splitOnLatitude);
        }
    }


    public Point searchNeighbour(Point point, Point best,  boolean splitOnLatitude) {
        if(node == null)
            return best;
        HeartDistance calc = new HeartDistance();
        double distance = calc.calculate(node, point);
        if (distance < calc.calculate(point, best))
            best = node;
        if(splitOnLatitude) {
            if(point.getLatitude() < node.getLatitude()) {
                best = leftChild.searchNeighbour(point, best, !splitOnLatitude);
                if(calc.calculate(point, best) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return rightChild.searchNeighbour(point, best, !splitOnLatitude);
            } else {
                best = rightChild.searchNeighbour(point, best, !splitOnLatitude);
                if(calc.calculate(point, best) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return leftChild.searchNeighbour(point, best, !splitOnLatitude);
            }
        } else {
            if(point.getLongitude() < node.getLongitude()) {
                best = leftChild.searchNeighbour(point, best, !splitOnLatitude);
                if(calc.calculate(point, best) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return rightChild.searchNeighbour(point, best, !splitOnLatitude);
            } else {
                best = rightChild.searchNeighbour(point, best, !splitOnLatitude);
                if(calc.calculate(point, best) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return leftChild.searchNeighbour(point, best, !splitOnLatitude);
            }
        }
        return best;
    }

    public List<Point> searchNeighbours(Point point, List<Point> bests, List<Double> distances, boolean splitOnLatitude, int size) {
        if(node == null)
            return bests;
        HeartDistance calc = new HeartDistance();
        double distance = calc.calculate(node, point);
        if(distances.size() < size) {
            bests.add(node);
            distances.add(distance);
        } else {
            double worse = getWorse(distances);
            if(distance < worse) {
                int index = distances.indexOf(worse);
                distances = substituteDist(distances, distance, index);
                bests = substituteNode(bests, node, index);
            }
        }
        if(splitOnLatitude) {
            if(point.getLatitude() < node.getLatitude()) {
                bests = leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(getWorse(distances) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            } else {
                bests = rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(getWorse(distances) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            }
        } else {
            if(point.getLongitude() < node.getLongitude()) {
                bests = leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(getWorse(distances) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            } else {
                bests = rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(getWorse(distances) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            }
        }
        return bests;
    }

    public List<Point> searchNeighbours(Point point, List<Point> bests, List<Double> distances, boolean splitOnLatitude, int size, double angle) {
        if(node == null)
            return bests;
        HeartDistance calc = new HeartDistance();
        if(AngleCalculator.isInRange(angle, AngleCalculator.getAngle(point, node))) {
            double distance = calc.calculate(node, point);
            if (distances.size() < size) {
                bests.add(node);
                distances.add(distance);
            } else {
                double worse = getWorse(distances);
                if (distance < worse) {
                    int index = distances.indexOf(worse);
                    distances = substituteDist(distances, distance, index);
                    bests = substituteNode(bests, node, index);
                }
            }
        }
        if(splitOnLatitude) {
            if(point.getLatitude() < node.getLatitude()) {
                bests = leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(bests.size() < size || getWorse(distances) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            } else {
                bests = rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(bests.size() < size || getWorse(distances) - calc.calculate(point, new Point(node.getLatitude(), point.getLongitude())) > 0)
                    return leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            }
        } else {
            if(point.getLongitude() < node.getLongitude()) {
                bests = leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(bests.size() < size || getWorse(distances) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            } else {
                bests = rightChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
                if(bests.size() < size || getWorse(distances) - calc.calculate(point, new Point(point.getLatitude(), node.getLongitude())) > 0)
                    return leftChild.searchNeighbours(point, bests, distances, !splitOnLatitude, size);
            }
        }
        return bests;
    }


    private double getWorse(List<Double> distances) {
        double biggerDistance = distances.get(0);
        for(int i = 1; i < distances.size(); i++)
            if(distances.get(i) > biggerDistance)
                biggerDistance = distances.get(i);
        return biggerDistance;
    }

    private List<Double> substituteDist(List<Double> list, double obj, int index) {
        list.remove(index);
        list.add(obj);
        return list;
    }

    private List<Point> substituteNode(List<Point> list, Point obj, int index) {
        list.remove(index);
        list.add(obj);
        return list;
    }

    public Point getNode() {
        return this.node;
    }
}
