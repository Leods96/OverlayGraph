package controllers.exceptions;

import graph_hopper.ControllerGH;
import input_output.exceptions.CheckPointException;
import overlay_matrix_graph.MatrixOverlayGraphManager;

import java.io.IOException;

import static controllers.Controller.*;

public class OverlayComputationController {

    private String inputPath;
    private String graphName;
    private boolean useCheckPoint;
    private int index;

    public OverlayComputationController(String inputPath, int index,  String graphName, boolean useCheckPoint) {
        this.graphName = graphName;
        this.inputPath = inputPath;
        this.useCheckPoint = useCheckPoint;
        this.index = index;
    }

    public void startComputation() throws InputFileException, GraphCreationException, CheckPointException, IOException {
        ControllerGH controllerGH;
        try {
            controllerGH = new ControllerGH(inputPath, index, inputPath, index,
                    dumpsPath + "\\" + graphName, useCheckPoint ? cpPath : null);
        } catch (IOException e) {
            throw new InputFileException();
        }
        controllerGH.computeDump();
        MatrixOverlayGraphManager graphManager = new MatrixOverlayGraphManager();
        //TODO manage the kd selection
        try {
            graphManager.loadOrCreateGraph(graphPath + "\\" + graphName, dumpsPath + "\\" + graphName, false);
        } catch (IOException | ClassNotFoundException e) {
            throw new GraphCreationException(e);
        }
    }
}
