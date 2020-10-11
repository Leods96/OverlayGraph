package util;

import input_output.ExcelReader;
import objects.Point;
import input_output.exceptions.CellTypeException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BoundaryNodesFilter {

    private static final int DISTANCE_THRESHOLD = 5000;

    public static void boundaryNodesFilterOnDistance(String dirPath) throws IOException, CellTypeException {
        ArrayList<Point> inputPoints;
        ArrayList<Point> outputPoints;
        HeartDistance calculator = new HeartDistance();
        File folder = new File(dirPath + "\\FilteredNodes");
        File[] files = folder.listFiles();
        for (File f : files) {
            ExcelReader reader = new ExcelReader(f.getAbsolutePath()).setSheetWithIndex(0).initializeIterator();
            inputPoints = new ArrayList<>();
            outputPoints = new ArrayList<>();
            while(reader.nextRow())
                inputPoints.add(new Point(reader.getID(), reader.getLatitude(), reader.getLongitude()));
            for(Point input : inputPoints) {
                boolean add = true;
                for(Point output : outputPoints) {
                    if(calculator.calculate(input, output) < DISTANCE_THRESHOLD) {
                        add = false;
                        break;
                    }
                }
                if(add) {
                    outputPoints.add(input);
                }
            }
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                XSSFSheet sheet = workbook.createSheet("result");
                Row row = sheet.createRow(0);
                row.createCell(0).setCellValue("ID");
                row.createCell(1).setCellValue("Lat");
                row.createCell(2).setCellValue("Lon");
                for (int i = 0; i < outputPoints.size(); i++) {
                    row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(outputPoints.get(i).getCode());
                    row.createCell(1).setCellValue(outputPoints.get(i).getLatitude());
                    row.createCell(2).setCellValue(outputPoints.get(i).getLongitude());
                }
                try (FileOutputStream outputStream = new FileOutputStream(dirPath + "\\FilteredNodes\\Filtered" + f.getName())) {
                    workbook.write(outputStream);
                }
            }
        }
    }

    public static void boundaryNodesFilterOnBoundary(String dirPath) throws IOException, CellTypeException {
        HashMap<String, Integer> nodes = new HashMap<>();
        ArrayList<Point> points = new ArrayList<>();
        File folder = new File(dirPath + "\\Nodes");
        File[] files = folder.listFiles();
        for (File f : files) {
            ExcelReader reader = new ExcelReader(f.getAbsolutePath()).setSheetWithIndex(0).initializeIterator();
            while(reader.nextRow()) {
                nodes.compute(reader.getID(), (k, v) -> (v == null) ? 1 : v + 1);
                if (nodes.get(reader.getID()) == 2)
                    points.add(new Point(reader.getID(), reader.getLatitude(), reader.getLongitude()));
            }
        }
        //nodes = (HashMap<String, Integer>) nodes.entrySet().stream().filter(x -> x.getValue() > 1).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("result");
            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("ID");
            row.createCell(1).setCellValue("Lat");
            row.createCell(2).setCellValue("Lon");
            for (int i = 0; i < points.size(); i++) {
                row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(points.get(i).getCode());
                row.createCell(1).setCellValue(points.get(i).getLatitude());
                row.createCell(2).setCellValue(points.get(i).getLongitude());
            }
            try (FileOutputStream outputStream = new FileOutputStream(dirPath + "\\FilteredNodes\\FilteredResult.xlsx")) {
                workbook.write(outputStream);
            }
        }
    }

    public static int countNodes(String dirPath) throws IOException {
        File folder = new File(dirPath);
        File[] files = folder.listFiles();
        int nodesCount = 0;
        for (File f : files) {
            System.out.println("File: " + f.getName() + " has " + (new ExcelReader(f.getAbsolutePath()).setSheetWithIndex(0).getNumberOfRowsInSheet()));
            nodesCount += new ExcelReader(f.getAbsolutePath()).setSheetWithIndex(0).getNumberOfRowsInSheet();
        }
        return nodesCount;
    }

    public static String produceStringForRequest(String dirPath) throws IOException, CellTypeException {
        StringBuilder sb = new StringBuilder();
        File folder = new File(dirPath);
        File[] files = folder.listFiles();
        sb.append("(");
        for (File f : files) {
            ExcelReader reader = new ExcelReader(f.getAbsolutePath()).setSheetWithIndex(0).initializeIterator();
            while(reader.nextRow())
                sb.append("node(").append(reader.getID()).append(");");
        }
        sb.append(");out geom;");
        return sb.toString();
    }

    public static String produceStringForRequest(HashMap<String, Integer> map) throws IOException, CellTypeException {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (String s : map.keySet())
            sb.append("node(").append(s).append(");");
        sb.append(");out geom;");
        return sb.toString();
    }
}
