package overlay_matrix_graph;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class OverlayHighwayExitTest extends TestCase {

    private final String FROM_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\HighwayExits.xlsx";
    private final String TO_FILE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\HighwayExits.xlsx";
    private final String DUMP_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\ExitsDumps\\";
    private final String CONFIG_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\checkPoint\\";
    private final String GRAPH_FOLDER = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\";
    private final int FROM_SHEET_NUM = 0;
    private final int TO_SHEET_NUM = 0;

    private final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";
    private final String PATH_TO_WRITE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\HighwayExitTest\\highwayExitOnDepotResult.xlsx";

    public void testOverlayHighwayExit() {
        MatrixOverlayGraphManager graphManager = null;
        try {
            //Util.creationOfDump(FROM_FILE, FROM_SHEET_NUM, TO_FILE, TO_SHEET_NUM, DUMP_FOLDER, CONFIG_FOLDER);
            graphManager = Util.creationOfOverlayGraph(DUMP_FOLDER, GRAPH_FOLDER, false);
        } catch (Exception e) {
            System.out.println("Test: ERROR");
            e.printStackTrace();
            fail();
        }
        ArrayList<Object[]> result = new ArrayList<>();
        ArrayList<Point> nodes = new ArrayList<>();
        GraphHopperInstance gh = new GraphHopperInstance();
        gh.preprocessing();

        XSSFWorkbook workbook = null;
        try(
                FileInputStream file = new FileInputStream(new File(PATH_TO_READ));
        ){
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }

        System.out.println("Test: Reading the input files");
        int rowCount = 0;
        XSSFSheet sheet = workbook.getSheetAt(1); //Take the depot
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        while(iterator.hasNext()) {
            Row row = iterator.next();
            try {
                nodes.add(new Point(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getNumericCellValue(),
                        row.getCell(2).getNumericCellValue()));
            } catch (NullPointerException e) {
                break;
            }
            rowCount++;
        }
        System.out.println("Test: Computing the routes");
        HeartDistance haversine = new HeartDistance();
        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    double ghDist = gh.routing(origin, destination).getDistance();
                    double ogDist = 0.0;
                    double haversineDist = haversine.calculate(origin, destination);
                    try {
                        ogDist = graphManager.route(origin, destination).getDistance();
                    } catch (NodeCodeNotInOverlayGraphException e) {
                        System.err.println("Error with graph's codes, origin: " + origin.getCode() + " dest: " + destination.getCode());
                    }
                    Object[] array = new Object[8];
                    array[0] = origin.getCode(); //Origin code
                    array[1] = destination.getCode(); //Dest code
                    array[2] = (int) ogDist; //Overlay distance
                    array[3] = (int) ghDist; //Graph hopper distance
                    array[4] = (int) ghDist - (int) ogDist;  //difference
                    if((int)array[4] < 0)
                        array[4] = (int) array[4] * -1;
                    array[5] = ghDist/ogDist;  //Rat
                    array[6] = haversineDist; // Haversine distance
                    array[7] = ogDist / haversineDist; //Rat between og and haversine
                    result.add(array);
                }
            }
        }
        System.out.println("Test: Ordering of the results");
        Comparator<Object[]> comp = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Integer)o1[4]).compareTo((Integer)o2[4]);
            }
        };

        Collections.sort(result, comp);

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
        row.createCell(13).setCellValue("Haversine");
        row.createCell(14).setCellValue("OG/Hav");

        for(int i = 0; i < result.size(); i++) {
            Object[] o = result.get(i);
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
            row.createCell(13).setCellValue((int) o[6]);
            row.createCell(14).setCellValue((double) o[7]);
        }

        //Formulas
        ArrayList<String> formulas = new ArrayList<>();
        formulas.add("Max Rat");
        formulas.add("MAX(L2:L" + (result.size() + 1) + ")");
        formulas.add("Min Rat");
        formulas.add("MIN(L2:L" + (result.size() + 1) + ")");
        formulas.add("Avg Rat");
        formulas.add("AVERAGE(L2:L" + (result.size() + 1) + ")");

        int j = 2;
        for(int i = 0; i < formulas.size(); i = i + 2) {
            row = sheet.getRow(j);
            Cell cell = row.createCell(17);
            cell.setCellValue(formulas.get(i));
            cell = row.createCell(18);
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
        assertTrue(true);
    }
}
