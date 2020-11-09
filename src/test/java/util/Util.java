package util;

import graph_hopper.ControllerGH;
import objects.Point;
import overlay_matrix_graph.MatrixOverlayGraphManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
    public static List<Point> generateRandomPoints(int dimension) {
        List<Point> points = new ArrayList<>();
        Random random = new Random();
        for(int i = 0; i < dimension; i++)
            points.add(new Point(String.valueOf(i), random.nextDouble()*100, random.nextDouble()*100));
        return points;
    }

    public static List<Point> generateRandomPoints(int dimension, int latMin, int latMax, int lonMin, int lonMax) {
        List<Point> points = new ArrayList<>();
        Random random = new Random();
        int latDiff = latMax - latMin;
        int lonDiff = lonMax - lonMin;
        for(int i = 0; i < dimension; i++)
            points.add(new Point(String.valueOf(i),latMin + latDiff * random.nextDouble(),
                    lonMin + lonDiff * random.nextDouble()));
        return points;
    }

    public static void creationOfDump(String fromFile, int fromSheetNum,
                                              String toFile, int toSheetNum, String dumpFolder,
                                              String configFolder) {
        try {
            ControllerGH controller = new ControllerGH(fromFile, fromSheetNum, toFile,
                    toSheetNum, dumpFolder, configFolder);
            System.out.println("Starting with the dump computation...");
            controller.computeDump(true);
            System.out.println("Dump computed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MatrixOverlayGraphManager creationOfOverlayGraph(String dumpFolder, String graphFolder, boolean tree) {
        MatrixOverlayGraphManager graphManager = null;
        System.out.println("Starting with the graph creation...");
        graphManager = new MatrixOverlayGraphManager();
        try {
            graphManager.loadOrCreateGraph(graphFolder, dumpFolder, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("OverlayGraph created");
        return graphManager;
    }
}
