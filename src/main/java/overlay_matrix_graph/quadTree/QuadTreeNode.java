package overlay_matrix_graph.quadTree;

import clusterization.*;
import location_iq.Point;
import util.EuclideanDistance;

import java.util.ArrayList;
import java.util.List;

public class QuadTreeNode {
    private static final int NUMBER_OF_CLUSTERS = 4;
    private static final int K_MEANS_ITERATION = 100;
    private ArrayList<Centroid> centroids; //Only for Node
    private ArrayList<QuadTreeNode> sons; //Only for Node
    private List<Point> points; //Only for Leaf
    private boolean leaf;

    /**
     * Given the List of Points create a quadTree
     * @param records List of points to be clustered
     */
    public QuadTreeNode(List<Point> records) {
        if(records.size() < NUMBER_OF_CLUSTERS) {
            createLeaf(records);
        } else {
            createCentralNode(records);
        }
    }

    /**
     * Create a QuadTree Leaf
     * @param records List of points into the final cluster
     */
    private void createLeaf(List<Point> records) {
        this.leaf = true;
        this.points = records;
    }

    /**
     * Create a QuadTree Node
     * @param records List of points to be clustered
     */
    private void createCentralNode(List<Point> records) {
        this.leaf = false;
        this.centroids = new ArrayList<>();
        this.sons = new ArrayList<>();
        ClusterSet clusterSet = KMeans.compute(records, NUMBER_OF_CLUSTERS,
                new EuclideanDistance(), K_MEANS_ITERATION);
        clusterSet.getClusterSet().forEach((centroid,listOfPoints) -> {
            this.centroids.add(centroid);
            this.sons.add(new QuadTreeNode(listOfPoints));
        });
    }

    /**
     * Recursive search of the neighbour of point into the QuadTree
     * @param point Point on which execute the research
     * @return The node in the graph nearest to point
     */
    public Point searchNeighbour(Point point) {
        if(leaf)
            return searchNeighbourIntoLeaf(point);
        else
            return searchNeighbourIntoNode(point);
    }

    /**
     * Final step of the recursive research
     * Calculate the distance with all the node in the cluster (Max NUMBER_OF_CLUSTERS node)
     * and return the nearest with respect to Euclidean Distance
     * @param point Point on which execute the research
     * @return The node in the graph nearest to point
     */
    private Point searchNeighbourIntoLeaf(Point point) {
        double minDistance = Double.MAX_VALUE;
        Point neigbour = null;
        for(Point p : points){
            double temp = new EuclideanDistance().calculate(point,p);
            if(minDistance > temp) {
                neigbour = p;
                minDistance = temp;
            }
        }
        return neigbour;
    }

    /**
     * Intermediate step of the recursive research
     * Calculate the EuclideanDistance with respect to each centroid of the clusters and forward
     * the research only into the best cluster
     * @param point Point on which execute the research
     * @return The node in the graph nearest to point
     */
    private Point searchNeighbourIntoNode(Point point) {
        int bestCentroidIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for(int i = 0; i < centroids.size(); i++) {
            double temp = new EuclideanDistance().calculate(centroids.get(i), point);
            if(minDistance > temp) {
                bestCentroidIndex = i;
                minDistance = temp;
            }
        }
        return sons.get(bestCentroidIndex).searchNeighbour(point);
    }
}
