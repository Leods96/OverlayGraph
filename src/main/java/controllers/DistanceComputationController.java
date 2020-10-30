package controllers;

import controllers.exceptions.GraphLoadingException;
import controllers.exceptions.OutputFileException;
import controllers.exceptions.ResultEmptyException;
import graph_hopper.GraphHopperInstance;
import input_output.MatrixExcelWriter;
import input_output.OutputFormat;
import objects.DistanceObject;
import objects.Point;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import overlay_matrix_graph.MatrixOverlayGraphManager;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static controllers.Controller.workDirPath;

public class DistanceComputationController {

    private String input;
    private String graphName;
    private int sheetIndex;
    private OutputFormat outputFormat;
    private boolean doubleComputation;
    private String outputName;

    private GraphHopperInstance gh;
    private MatrixOverlayGraphManager og;

    private boolean usingGH;

    public DistanceComputationController(String input, int sheetIndex, String graphName, OutputFormat outputFormat, boolean doubleComputation, String outputName) {
        this.input = input;
        this.outputFormat = outputFormat;
        this.graphName = graphName;
        this.sheetIndex = sheetIndex;
        this.doubleComputation = doubleComputation;
        this.usingGH = graphName.equals("graphhopper"); //TODO check if correct
        this.outputName = outputName;
    }

    public void process() throws IOException, ResultEmptyException, GraphLoadingException, OutputFileException {
        List <Point> nodes = externalFileParsing();
        try {
            loadGraph();
        } catch (IOException | ClassNotFoundException e) {
            throw new GraphLoadingException(e);
        }
        List <DistanceObject> result = computeDistances(nodes);
        if (result.isEmpty())
            throw new ResultEmptyException();
        printResult(result);
    }

    private List<Point> externalFileParsing() throws IOException {
        XSSFWorkbook workbook;
        try(FileInputStream file = new FileInputStream(new File(input)))
        {
            workbook = new XSSFWorkbook(file);
        }
        if (workbook.getNumberOfSheets() < sheetIndex || sheetIndex < 1)
            throw new ArrayIndexOutOfBoundsException(sheetIndex);
        XSSFSheet sheet = workbook.getSheetAt(sheetIndex - 1);
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        ArrayList<Point> nodes = new ArrayList<>();
        while(iterator.hasNext()) {
            Row row = iterator.next();
            try {
                nodes.add(new Point(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getNumericCellValue(),
                        row.getCell(2).getNumericCellValue()));
            } catch (NullPointerException e) {
                System.out.println("The row " + row.getRowNum() + " of the input file skipped because contains some errors");
            }
        }
        return nodes;
    }

    private void loadGraph() throws IOException, ClassNotFoundException{
        if(usingGH) {
            gh = new GraphHopperInstance();
            gh.preprocessing(workDirPath + "\\Graphs\\" + graphName);
        } else {
            og = new MatrixOverlayGraphManager();
            og.setGraphPath(workDirPath + "\\Graphs\\" + graphName);
            og.loadGraph();
        }
    }

    private List<DistanceObject> computeDistances(List<Point> nodes) {
        ArrayList<DistanceObject> result = new ArrayList<>();
        for ( int o = 0; o < nodes.size(); o++ )
            for ( int d = (doubleComputation ? 0 : o + 1); d < nodes.size(); d++ ) {
                if (o == d)
                    continue;
                Point origin = nodes.get(o), destination = nodes.get(d);
                if(usingGH)
                    if(outputFormat == OutputFormat.EXCEL_FILE_LIST)
                        result.add(new DistanceObject(origin.getCode(), destination.getCode(), gh.routing(origin, destination).getDistance()));
                    else
                        result.add(new DistanceObject(origin.getCode(), gh.routing(origin, destination).getDistance()));
                else
                    try {
                        if (outputFormat == OutputFormat.EXCEL_FILE_LIST)
                            result.add(new DistanceObject(origin.getCode(), destination.getCode(), og.route(origin, destination).getDistance()));
                        else
                            result.add(new DistanceObject(origin.getCode(), og.route(origin, destination).getDistance()));
                    } catch (NodeCodeNotInOverlayGraphException e) {
                        //TODO questo non dovrebbe mai succedere controllare
                        System.err.println("The overlay graph is not able to find a correct neighbour");
                        System.err.println(e.getMessage());
                    }
            }
        return result;
    }

    private void printResult(List<DistanceObject> result) throws OutputFileException {
        MatrixExcelWriter.write(workDirPath + "\\Outputs\\" + outputName, result, outputFormat);
    }

}
