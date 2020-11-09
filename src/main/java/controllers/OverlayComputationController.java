package controllers;

import controllers.exceptions.GraphCreationException;
import controllers.exceptions.InputFileException;
import graph_hopper.ControllerGH;
import input_output.exceptions.CheckPointException;
import overlay_matrix_graph.MatrixOverlayGraphManager;

import java.io.File;
import java.io.IOException;

import static controllers.Controller.*;

public class OverlayComputationController {

    private String inputPath;
    private String graphName;
    private boolean useCheckPoint;
    private boolean useKdTree;
    private int index;

    public OverlayComputationController(String inputPath, int index,  String graphName, boolean useCheckPoint, boolean useKdTree) {
        this.graphName = graphName;
        this.inputPath = inputPath;
        this.useCheckPoint = useCheckPoint;
        this.useKdTree = useKdTree;
        this.index = index;
    }

    public void startComputation() throws InputFileException, GraphCreationException, CheckPointException, IOException {
        ControllerGH controllerGH;

        try {
            controllerGH = new ControllerGH(inputPath, index, inputPath, index,
                    dumpsPath + "\\" + graphName + "\\", useCheckPoint ? cpPath : null);
        } catch (IOException e) {
            throw new InputFileException();
        }
        new File(dumpsPath + "\\" + graphName).mkdir();
        try {
            controllerGH.computeDump(useCheckPoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MatrixOverlayGraphManager graphManager = new MatrixOverlayGraphManager();
        try {
            graphManager.loadOrCreateGraph(graphPath + "\\" + graphName, dumpsPath + "\\" + graphName, useKdTree);
        } catch (IOException | ClassNotFoundException e) {
            new File(graphPath + "\\" + graphName).delete();
            throw new GraphCreationException(e);
        }
    }
}
