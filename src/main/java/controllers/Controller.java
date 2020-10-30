package controllers;

import controllers.exceptions.*;
import input_output.ExternalFileManager;
import input_output.OutputFormat;
import input_output.exceptions.CheckPointException;
import input_output.exceptions.FileInWrongFormatException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Controller {

    private final ExternalFileManager fileManager;

    //TODO manage this var
    public static String workDirPath;
    public static final String cpPath = workDirPath + "\\CheckPoint";
    public static final String graphPath = workDirPath + "\\Graphs";
    public static final String ghPath = graphPath + "\\GraphHopper";
    public static final String dumpsPath = workDirPath + "\\Dumps";

    //Vars for the distance computation
    private String inputAddressFile;
    private String graphName;
    private int sheetIndex;
    private OutputFormat outputFormat;
    private boolean doubleComputation;
    private String outputName;

    //Vars for the overlay creation
    private boolean useCheckPoint;


    public Controller() {
        fileManager = new ExternalFileManager(workDirPath);
        //TODO load from a file all the necessary data
        //precomputazioni salvate
        //file di configurazione da non reinserire
    }

    /**
     * This method set the path of the file from which read the input addresses
     * @param path String containing the absolute path
     * @throws Exception raised if the file does not exist or is not in the correct format
     */
    public void setInputAddressFile(String path) throws FileNotFoundException, FileInWrongFormatException {
        fileManager.fileExists(path);
        fileManager.fileInCorrectFormat(path, new String[]{"xlsx"}); //only excel file are accepted
        this.inputAddressFile = path;
    }

    public void setOutputName(String name) {
        //TODO check if this name already exist
        this.outputName = name;
    }

    /**
     * This method read the directory containing the graphs and return a list of them identified by name
     * @return List of strings
     */
    public List<String> getExistingGraphs() {
        return fileManager.getGraphs();
    }

    /**
     * This method set the graph between those retrieved from the graphs folder
     * @param graphName name of the graph
     */
    public void setGraphToUse(String graphName) {
        this.graphName = graphName;
    }

    /**
     * This method set the format of the distance matrix
     * @param outputFormat one between all of the available formats
     */
    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * This method set the index of the workbook's sheet
     * @param sheetIndex
     */
    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex - 1;
    }

    /**
     * This method set the doubleComputation variable that specifies if the distance matrix will approximates the
     * distance between one way or the skip of the direction
     * @param doubleComputation
     */
    public void setRoundTripOption(boolean doubleComputation) {
        this.doubleComputation = doubleComputation;
    }

    /**
     * Perform the computation based on the previous inputs
     */
    public void startComputation() throws IOException, ResultEmptyException, GraphLoadingException, OutputFileException {
        if(outputFormat == OutputFormat.TRIANGULAR_DISTANCE_MATRIX)
            doubleComputation = false;
        if(outputFormat == OutputFormat.SQUARE_DISTANCE_MATRIX)
            doubleComputation = true;
        DistanceComputationController distanceComputationController = new DistanceComputationController(inputAddressFile, sheetIndex, graphName, outputFormat, doubleComputation, outputName);
        distanceComputationController.process();
    }

    /**
     * Perform the overlay graph pre-computation based on the previous inputs
     */
    public void startOverlayPreComputation() throws IOException, InputFileException, GraphCreationException, CheckPointException {
        OverlayComputationController overlayController = new OverlayComputationController(inputAddressFile, sheetIndex, graphName, useCheckPoint);
        overlayController.startComputation();
    }

    /**
     * This method set the path of the file from which read the OSM data
     * @param path String containing the absolute path
     * @throws Exception raised if the file does not exist or is not in the correct format
     */
    public void setOSMPath(String path) throws Exception{

    }

    /**
     * This method set the name of the overlay graph that will be created
     * @param name String containing the name of the graph
     * @throws Exception raised if the name is already used
     */
    public void setOverlayGraphName(String name) throws Exception {
        //TODO check the name is not already used
        this.graphName = name;
    }

    /**
     * Enable the use of checkpoints
     */
    public void enableCheckPoint() {
        this.useCheckPoint = true;
    }

    /**
     * Disable the use of checkpoints
     */
    public void disableCheckPoint() {
        this.useCheckPoint = false;
    }

    /**
     * This method return the path into which the graph are currently saved
     * @return String representing the path, null if no path is set
     */
    public String getGraphPath() {
        return null;
    }

    /**
     * This method set the path of the folder where the graphs will be saved
     * @param path String containing the absolute path
     * @throws Exception raised if the folder does not exist
     */
    public void setGraphFolderPath(String path) throws Exception {

    }

    /**
     * Execute the creation of the graph-hopper graph based on the OSM file
     * @throws Exception problem during the graph-hopper creation
     */
    public void graphHopperGraphCreation() throws Exception {

    }

    /**
     * This method will check if there is a check-point for the creation of an overlay graph
     * @return true if exist a check point for a previous computation, false if there is not
     */
    public boolean checkOverlayCreationInProgress() {
        return true;
    }

    /**
     * Overlay pre-computation will restart from the saved check point
     */
    public void reStartOverlayCreation() {

    }

    /**
     * Method used to delete a graph based on his index
     */
    public void deleteGraph() {
        fileManager.deleteGraph(graphName);
    }

}
