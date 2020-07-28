package overlay_matrix_graph;

import graph_hopper.GraphHopperInstance;
import junit.framework.TestCase;
import location_iq.Point;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import overlay_matrix_graph.exceptions.NodeCodeNotInOverlayGraphException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class graphCreationTest extends TestCase {
    private final String fromFile = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\Milano-Roma\\supportGraphMilanoRoma.xlsx";
    private final String toFile = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\Milano-Roma\\supportGraphMilanoRoma.xlsx";
    private final String dumpFolder = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\GHDumpFolder\\MilanoRomaExternal\\";
    private final String configFolder = null;
    private final int fromSheetNum = 0;
    private final int toSheetNum = 0;
    private final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\Milano-Roma\\supportInternalMilanoRoma.xlsx";
    private final String PATH_TO_WRITE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\Milano-Roma\\supportInternalMilanoRomaResult.xlsx";


    public void testGraphCreation() {
        MatrixOverlayGraphManager graphManager = null;
        try {
            /*ControllerGH controller = new ControllerGH(fromFile, fromSheetNum, toFile, toSheetNum, dumpFolder, configFolder);
            System.out.println("Starting with the dump computation...");
            controller.computeDump();
            System.out.println("Dump computed");
            System.out.println("Starting with the graph creation...");*/
            graphManager = new MatrixOverlayGraphManager();
            graphManager.loadOrCreateGraph("C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\", dumpFolder);
        }catch (Exception e) {
            System.out.println("ERROR");
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
            System.err.println("Error opening the file");
            fail();
        }

        int rowCount = 0;
        XSSFSheet sheet = workbook.getSheetAt(0);
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

        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j && nodes.get(i).getCode().charAt(1) != (nodes.get(j).getCode().charAt(1))) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    double ghDist = gh.routing(origin, destination).getDistance();
                    double ogDist = 0.0;
                    try {
                        ogDist = graphManager.route(origin, destination).getDistance();
                    } catch (NodeCodeNotInOverlayGraphException e) {
                        System.err.println("Error with graph's codes, origin: " + origin.getCode() + " dest: " + destination.getCode());
                    }
                    //haversineDist = markFormula(haversineDist);
                    Object[] array = new Object[6];
                    array[0] = origin.getCode();
                    array[1] = destination.getCode();
                    array[2] = (int) ogDist;
                    array[3] = (int) ghDist;
                    array[4] = (int) ghDist - (int) ogDist;
                    if((int)array[4] < 0)
                        array[4] = (int) array[4] * -1;
                    array[5] = ghDist/ogDist;
                    result.add(array);
                }
            }
        }

        Comparator<Object[]> comp = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Integer)o1[4]).compareTo((Integer)o2[4]);
            }
        };

        Collections.sort(result, comp);

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
        }

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
