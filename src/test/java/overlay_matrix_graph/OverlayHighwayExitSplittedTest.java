package overlay_matrix_graph;

import com.graphhopper.PathWrapper;
import graph_hopper.GraphHopperInstance;
import junit.framework.TestCase;
import location_iq.Point;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;
import util.HeartDistance;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OverlayHighwayExitSplittedTest extends TestCase {

    private final String FROM_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private final String TO_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private final String DUMP_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\ProvinceBoudariesDump\\";
    private final String CONFIG_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\checkPoint\\";
    private final String GRAPH_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private final int FROM_SHEET_NUM = 0;
    private final int TO_SHEET_NUM = 0;

    private final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";
    private final String PATH_TO_WRITE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\ProvinceBoundaries\\Angle-NoHav-Inc1,45.xlsx";

    public void testOverlayHighwayExit() {
        ArrayList<Point> nodes = new ArrayList<>();
        HashMap<String, Object[]> result = new HashMap<>();

        System.out.println("TEST: Reading of the input");
        XSSFWorkbook workbook = null;
        try(
                FileInputStream file = new FileInputStream(new File(PATH_TO_READ));
        ){
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }
        int rowCount = 0;
        XSSFSheet sheet = workbook.getSheetAt(1); //Take the customer
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        //int iterationCounter = 1;
        while(iterator.hasNext() && rowCount < 500) {
        //while(iterationCounter < 19000) {
            Row row = iterator.next();
            //Row row = sheet.getRow(iterationCounter);
            //iterationCounter += 190;
            try {
                nodes.add(new Point(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getNumericCellValue(),
                        row.getCell(2).getNumericCellValue()));
            } catch (NullPointerException e) {
                continue;
            }
            rowCount++;
        }

        System.out.println("TEST: creation of overlay graph");
        long time = System.nanoTime();
        MatrixOverlayGraphManager graphManager = null;
        try {
            //Util.creationOfDump(FROM_FILE, FROM_SHEET_NUM, TO_FILE, TO_SHEET_NUM, DUMP_FOLDER, CONFIG_FOLDER);
            graphManager = Util.creationOfOverlayGraph(DUMP_FOLDER, GRAPH_FOLDER, false);
        } catch (Exception e) {
            System.out.println("Test: ERROR creating the overlay graph");
            e.printStackTrace();
            fail();
        }
        System.out.println("TEST: Graph loaded in "+ (System.nanoTime()-time)/1000000000);
        System.out.println("TEST: Computing the routes for the overlay graph");
        time = System.nanoTime();
        long overlayTime = System.nanoTime();
        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    double ogDist = 0.0;
                    OverlayResponse response = null;
                    try {
                        response = graphManager.route(origin, destination);
                        ogDist = response.getDistance();
                    } catch (NodeCodeNotInOverlayGraphException e) {
                        System.err.println("Test: Error with graph's codes, origin: " + origin.getCode() + " dest: " + destination.getCode());
                    }
                    Object[] array = new Object[9];
                    array[0] = origin.getCode(); //Origin code
                    array[1] = destination.getCode(); //Dest code
                    array[2] = (int) ogDist; //Overlay distance
                    StringBuilder sb = new StringBuilder();
                    sb.append("https://www.google.nl/maps/dir");
                    if(response.getMiddlePath()) {
                        sb.append("/").append(response.getOrigin().getLatitude()).append(",+").append(response.getOrigin().getLongitude());
                        sb.append("/").append(response.getOriginNeighbour().getLatitude()).append(",+").append(response.getOriginNeighbour().getLongitude());
                        sb.append("/").append(response.getDestinationNeighbour().getLatitude()).append(",+").append(response.getDestinationNeighbour().getLongitude());
                        sb.append("/").append(response.getDestination().getLatitude()).append(",+").append(response.getDestination().getLongitude());
                    }
                    array[8] = sb.toString();
                    result.put(computeIdFromCode(origin,destination),array);
                }
            }
        }
        System.out.println("TEST: computation done in "+ (System.nanoTime()-time)/1000000000);
        overlayTime = System.nanoTime()-time;
        graphManager = null;
        System.gc(); //cleaning of the memory
        System.out.println("TEST: Computing Haversine distance");
        time = System.nanoTime();
        HeartDistance haversine = new HeartDistance();
        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    double haversineDist = haversine.calculate(origin, destination);
                    Object[] array = result.get(computeIdFromCode(origin,destination));
                    array[6] = (int) haversineDist; // Haversine distance
                    array[7] = ((int) array[2]) / haversineDist; //Rat between og and haversine
                    result.put(computeIdFromCode(origin,destination),array);
                }
            }
        }
        System.out.println("TEST: computation done in "+ (System.nanoTime()-time)/1000000000);
        System.out.println("TEST: creation of graph hopper graph");
        GraphHopperInstance gh = new GraphHopperInstance();
        gh.preprocessing();
        System.out.println("TEST: Computing the routes for the graph hopper graph");
        time = System.nanoTime();
        long ghTime = System.nanoTime();
        PathWrapper response = null;
        double ghDist = 0;
        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    response = gh.routing(origin, destination);
                    if(!response.hasErrors()) {
                        ghDist = gh.routing(origin, destination).getDistance();
                        Object[] array = result.get(computeIdFromCode(origin, destination));
                        array[3] = (int) ghDist; //Graph hopper distance
                        array[4] = (int) ghDist - (int) array[2];  //difference
                        if ((int) array[4] < 0)
                            array[4] = (int) array[4] * -1;
                        array[5] = ghDist / ((int) array[2]);  //Rat
                        result.put(computeIdFromCode(origin, destination), array);
                    } else {
                        result.remove(computeIdFromCode(origin, destination));
                        System.err.println("Response removed from the result beacuse contains errors!!" +
                                "\nFrom: " + origin + ", To: " + destination);
                    }
                }
            }
        }
        System.out.println("TEST: computation done in "+ (System.nanoTime()-time)/1000000000);
        ghTime = System.nanoTime() - ghTime;
        gh = null;
        System.gc();

        System.out.println("Test: Ordering of the results");
        Comparator<Object[]> comp = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Integer)o1[4]).compareTo((Integer)o2[4]);
            }
        };

        ArrayList<Object[]> res = new ArrayList<>(result.values());
        result = null;

        //Removing result without middle path
        res = new ArrayList(res.stream().filter(r -> !r[8].toString().equals("https://www.google.nl/maps/dir")).collect(Collectors.toList()));

        System.gc();
        Collections.sort(res, comp);

        System.out.println("Test: Creation of the output file");
        System.out.println("Row count = " + rowCount);
        int count = 1;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("result");

        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("code");
        row.createCell(1).setCellValue("Lat");
        row.createCell(2).setCellValue("Lng");
        row.createCell(5).setCellValue("From");
        row.createCell(6).setCellValue("To");
        row.createCell(7).setCellValue("Overlay");
        row.createCell(8).setCellValue("GH");
        row.createCell(10).setCellValue("Diff");
        row.createCell(11).setCellValue("Rat");
        row.createCell(12).setCellValue("Error");
        row.createCell(13).setCellValue("Haversine");
        row.createCell(14).setCellValue("OG/Hav");
        row.createCell(20).setCellValue("url");

        for(int i = 0; i < res.size(); i++) {
            Object[] o = res.get(i);
            row = sheet.createRow(count);
            if (count <= rowCount) {
                row.createCell(0).setCellValue(nodes.get(i).getCode());
                row.createCell(1).setCellValue(nodes.get(i).getLatitude());
                row.createCell(2).setCellValue(nodes.get(i).getLongitude());
            }
            count++;
            row.createCell(5).setCellValue((String)o[0]);
            row.createCell(6).setCellValue((String)o[1]);
            row.createCell(7).setCellValue((int) o[2]);
            row.createCell(8).setCellValue((int) o[3]);
            row.createCell(10).setCellValue((int) o[4]);
            row.createCell(11).setCellValue((double) o[5]);
            row.createCell(12).setCellFormula("(I"+count+"-H"+count+")/I"+count);
            row.createCell(12).setCellFormula("IF(((I"+count+"-H"+count+")/I"+count+" > 0), (I"+count+"-H"+count+")/I"+count+", -(I"+count+"-H"+count+")/I"+count+") * 100");
            row.createCell(13).setCellValue((int) o[6]);
            row.createCell(14).setCellValue((double) o[7]);
            row.createCell(20).setCellValue(o[8].toString());
        }

        //Formulas
        ArrayList<String> formulas = new ArrayList<>();
        formulas.add("Max Rat");
        formulas.add("MAX(L2:L" + (res.size() + 1) + ")");
        formulas.add("Min Rat");
        formulas.add("MIN(L2:L" + (res.size() + 1) + ")");
        formulas.add("Avg Rat");
        formulas.add("AVERAGE(L2:L" + (res.size() + 1) + ")");

        formulas.add("Max Error");
        formulas.add("MAX(M2:M" + (res.size() + 1) + ")");
        formulas.add("Min Error");
        formulas.add("MIN(M2:M" + (res.size() + 1) + ")");
        formulas.add("Avg Error");
        formulas.add("AVERAGE(M2:M" + (res.size() + 1) + ")");

        int j = 2;
        for(int i = 0; i < formulas.size(); i = i + 2) {
            row = sheet.getRow(j);
            Cell cell = row.createCell(16);
            cell.setCellValue(formulas.get(i));
            cell = row.createCell(17);
            cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
            cell.setCellFormula(formulas.get(i+1));
            j++;
        }

        System.out.println("Test: Write of the output file");
        try(FileOutputStream outputStream = new FileOutputStream(PATH_TO_WRITE))
        {
            workbook.write(outputStream);
        } catch (IOException e) {
            System.err.println("Error writing the file");
            fail();
        }

        System.out.println("\nSpeed-Up: " + ghTime/overlayTime);
        assertTrue(true);
    }

    private String computeIdFromCode(Point origin, Point destination) {
        return origin.getCode() + destination.getCode();
    }
}
