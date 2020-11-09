package controllers;

import controllers.exceptions.*;
import graph_hopper.GraphHopperInstance;
import input_output.ExternalCheckPointManager;
import input_output.ExternalFileManager;
import input_output.OutputFormat;
import input_output.exceptions.CheckPointException;
import input_output.exceptions.FileInWrongFormatException;
import objects.CheckPoint;
import objects.ParamsObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class Controller {
    private final ExternalFileManager fileManager;

    public static final String workDirPath = "Work_Directory";
    public static final String cpPath = workDirPath + "\\CheckPoint";
    public static final String graphPath = workDirPath + "\\Graphs";
    public static final String ghPath = graphPath + "\\GraphHopper";
    public static final String dumpsPath = workDirPath + "\\Dumps";
    public static final String outputPath = workDirPath + "\\Output";
    public static final String graphConfigPath = workDirPath + "\\GraphConfiguration";

    private String inputAddressFile;
    private String graphName;
    private String osmPath;
    private String outputName;

    private int sheetIndex;

    private OutputFormat outputFormat;

    private boolean doubleComputation;
    private boolean useCheckPoint;
    private boolean useKdTree;

    public Controller() {
        fileManager = new ExternalFileManager(workDirPath);
        if(!fileManager.fileExists(cpPath)) fileManager.createFolderFromPath(cpPath);
        if(!fileManager.fileExists(graphPath)) fileManager.createFolderFromPath(graphPath);
        if(!fileManager.fileExists(ghPath)) fileManager.createFolderFromPath(ghPath);
        if(!fileManager.fileExists(dumpsPath)) fileManager.createFolderFromPath(dumpsPath);
        if(!fileManager.fileExists(outputPath)) fileManager.createFolderFromPath(outputPath);
        if(!fileManager.fileExists(graphConfigPath)) fileManager.createFolderFromPath(graphConfigPath);
    }

    /**
     * This method set the path of the file from which read the input addresses
     * @param path String containing the absolute path
     * @throws Exception raised if the file does not exist or is not in the correct format
     */
    public void setInputAddressFile(String path) throws FileNotFoundException, FileInWrongFormatException {
        if(!fileManager.fileExists(path))
            throw new FileNotFoundException("The specified file does not exists");
        fileManager.fileInCorrectFormat(path, new String[]{"xlsx"}); //only excel file are accepted
        this.inputAddressFile = path;
    }

    public void setOutputName(String name) {
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
        if(useCheckPoint)
            new ExternalCheckPointManager().createCheckPointFileData(inputAddressFile, sheetIndex, graphName, useKdTree);
        OverlayComputationController overlayController = new OverlayComputationController(inputAddressFile, sheetIndex, graphName, useCheckPoint, useKdTree);
        try {
            overlayController.startComputation();
        } finally {
            new ExternalCheckPointManager().deleteCheckPointFile();
        }
    }

    /**
     * Overlay pre-computation will restart from the saved check point
     */
    public void reStartOverlayCreation() throws IOException, InputFileException, GraphCreationException, CheckPointException{
        ExternalCheckPointManager reader = new ExternalCheckPointManager();
        CheckPoint cp = reader.getFileData();
        this.inputAddressFile = cp.getInputFilePath();
        this.sheetIndex = cp.getFileIndex();
        this.graphName = cp.getGraphName();
        this.useKdTree = cp.getKdTree();
        this.useCheckPoint = true;
        startOverlayPreComputation();
    }

    /**
     * This method set the path of the file from which read the OSM data
     * @param path String containing the absolute path
     * @throws Exception raised if the file does not exist or is not in the correct format
     */
    public void setOSMPath(String path) throws FileNotFoundException, FileInWrongFormatException {
        if (!fileManager.fileExists(path))
            throw new FileNotFoundException();
        fileManager.fileInCorrectFormat(path, new String[]{"osm","pbf","osm.pbf"}); //only osm file are accepted
        this.osmPath = path;
    }

    /**
     * This method set the name of the overlay graph that will be created
     * @param name String containing the name of the graph
     * @throws GraphCreationException raised if the name is already used
     */
    public void setOverlayGraphName(String name) throws GraphCreationException {
        if (fileManager.getGraphs().contains(name))
            throw new GraphCreationException();
        this.graphName = name;
    }

    /**
     * Enable or disable the use of checkpoints
     * @param useCheckPoint
     */
    public void useCheckPoint(boolean useCheckPoint) {
        this.useCheckPoint = useCheckPoint;
    }

    /**
     * Enable the use of a tree as supporter
     * @param useKdTree
     */
    public void useKdTree(boolean useKdTree) {
        this.useKdTree = useKdTree;
    }

    /**
     * This method check if the graph folder exist
     * @return true if this folder exist, false otherwise
     */
    public boolean graphsFolderExist() {
        return fileManager.fileExists(graphPath);
    }

    /**
     * This method create folder where the graphs will be saved
     * @return true if the folder is created, false otherwise
     */
    public boolean graphsFolderSetUp() {
        return fileManager.createFolderFromPath(graphPath);
    }

    /**
     * Execute the creation of the graph-hopper graph based on the OSM file
     * @throws Exception problem during the graph-hopper creation
     */
    public void graphHopperGraphCreation() throws Exception {
        GraphHopperInstance gh = new GraphHopperInstance();
        gh.preprocessing(osmPath);
    }

    /**
     * This method will check if there is a check-point for the creation of an overlay graph
     * @return true if exist a check point for a previous computation, false if there is not
     */
    public boolean checkOverlayCreationInProgress() {
        return fileManager.dirContainsFiles(cpPath);
    }

    /**
     * Method used to delete a graph based on his index
     */
    public void deleteGraph() throws IOException {
        fileManager.deleteGraph(graphName);
    }

    public void createConfigurationFile(ParamsObject po) throws IOException {
        fileManager.createConfigFile(po);
    }

    public void deleteConfigurationFile() throws IOException{
        fileManager.deleteInnerFiles(graphConfigPath);
    }

}
