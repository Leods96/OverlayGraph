package overlay_matrix_graph.quadTree;

import clusterization.*;
import location_iq.Point;
import util.EuclideanDistance;

import java.util.ArrayList;
import java.util.List;

/**
 * Is the implementation of a quadTree: a tree into which each node can have four children, each node can be a
 * central node or a leaf and are distinguished by the boolean variable leaf.
 * The leafs contain a list of points.
 * The central nodes contain two arraylist: the first one identify the centroids and the second one the children
 * nodes, the connection between children and centroid is done through the index: each son is relative to a
 * specific centroid and the are at the same position into the arrays.
 */
public class QuadTreeNode {
    private static final int NUMBER_OF_CLUSTERS = 4;
    private static final int MAX_NUM_PER_CLUSTER = 8;
    /**
     * Maximum number of layers into the tree, if negative means no limits
     */
    private static final int MAX_LAYERS = 0;
    private static final int K_MEANS_ITERATION = 100;
    private ArrayList<Point> centroids; //Only for Node
    private ArrayList<QuadTreeNode> sons; //Only for Node
    private List<Point> points; //Only for Leaf
    private boolean leaf;

    /**
     * Given the List of Points create a quadTree in a recursive mood
     * @param records List of points to be clustered
     */
    public QuadTreeNode(List<Point> records) {
        if(records.size() < MAX_NUM_PER_CLUSTER) {
            createLeaf(records);
        } else {
            createCentralNode(records, 0);
        }
    }

    public QuadTreeNode(List<Point> records, int countLayers) {
        if(records.size() < MAX_NUM_PER_CLUSTER ||(MAX_LAYERS >= 1 && countLayers >= MAX_LAYERS)) {
            createLeaf(records);
        } else {
            createCentralNode(records, countLayers +1);
        }
    }

    /**
     * Create a QuadTree Leaf.
     * End point of the recursive construction.
     * @param records List of points into the final cluster
     */
    private void createLeaf(List<Point> records) {
        this.leaf = true;
        this.points = records;
    }

    /**
     * Create a QuadTree Node and forward the creation to the sons.
     * Performs the KMeans clusterization over the input points, each cluster will corresponds to a son.
     * @param records List of points to be clustered
     */
    private void createCentralNode(List<Point> records, int countLayers) {
        this.leaf = false;
        this.centroids = new ArrayList<>();
        this.sons = new ArrayList<>();
        ClusterSet clusterSet = KMeans.compute(records, NUMBER_OF_CLUSTERS,
                new EuclideanDistance(), K_MEANS_ITERATION);
        clusterSet.getClusterSet().forEach((centroid,listOfPoints) -> {
            this.centroids.add(centroid);
            this.sons.add(new QuadTreeNode(listOfPoints, countLayers + 1));
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

    @Override
    public String toString() {
        if(leaf)
            return "leaf - points: " + points;
        return "\nNode - centroids: " + centroids + sons;
    }

    public void printTree() {
        printTree(0);
    }

    public void printTree(int step) {
        if(leaf) {
            System.out.print("\n");
            for(int i = 0; i < step; i++)
                System.out.print("\t");
            System.out.print("Leaf - points: " + points);
        } else {
            for(int j = 0; j < centroids.size(); j++) {
                System.out.print("\n");
                for(int i = 0; i < step; i++)
                    System.out.print("\t");
                System.out.print("Centroid: " + centroids.get(j) + "Sons: " );
                sons.get(j).printTree(step + 1);
            }
        }
    }

}
