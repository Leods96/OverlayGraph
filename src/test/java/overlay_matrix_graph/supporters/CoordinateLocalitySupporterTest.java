package overlay_matrix_graph.supporters;

import junit.framework.TestCase;
import location_iq.Point;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CoordinateLocalitySupporterTest extends TestCase {

    private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private static final String PATH_TO_TEST = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";

    public void testCorrectnessOfCoordinateSupporter() {
        ArrayList<Point> points = new ArrayList<>();
        XSSFWorkbook workbook = null;
        try (FileInputStream file = new FileInputStream(new File(PATH_TO_READ)))
        {
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        while(iterator.hasNext()) {
            Row row = iterator.next();
            points.add(new Point(row.getCell(0).getStringCellValue(),
                    row.getCell(1).getNumericCellValue(),
                    row.getCell(2).getNumericCellValue()));
        }
        CoordinateLocalitySupporter cooSupp = null;
        try {
            cooSupp = new CoordinateLocalitySupporter(points, 10);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        LinearSupporter linear = new LinearSupporter(points);
        try (FileInputStream file = new FileInputStream(new File(PATH_TO_TEST)))
        {
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }
        List<Point> pointsToBeTested = new ArrayList<>();
        sheet = workbook.getSheetAt(0);
        iterator = sheet.rowIterator();
        iterator.next();
        while(iterator.hasNext()) {
            Row row = iterator.next();
            try {
                pointsToBeTested.add(new Point(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getNumericCellValue(),
                        row.getCell(2).getNumericCellValue()));
            } catch (Exception e) {
                continue;
            }
        }
        //List<Point> pointsToBeTested = Util.GenerateRandomPoints(100, 36, 46, 7, 18);
        int failedIntersection = 0;
        long time = System.nanoTime();
        for(Point pointToBeTested : pointsToBeTested) {
            List<Point> linearResult = linear.searchNeighbours(pointToBeTested, 4);
            time = System.nanoTime();
            List<Point> cooResult = cooSupp.searchNeighbours(pointToBeTested);

            int count = 0;
            for (Point linearPoint : linearResult)
                for (Point cooPoint : cooResult)
                    if (linearPoint.sameCoordinates(cooPoint))
                        count++;
            System.out.println("Point to be searched: " + pointToBeTested);
            if (count == 0) {
                System.err.println("No intersection between result");
                failedIntersection++;
            } else {
                System.out.println("Founded " + count + " good results");
            }
            System.out.println("Linear result: ");
            System.out.println(linearResult);
            System.out.println("Hamming result: " + cooResult.size() + " points");
            System.out.println(cooResult + "\n\n");
        }
        System.out.println("Failed intersection : " + failedIntersection);
    }

}