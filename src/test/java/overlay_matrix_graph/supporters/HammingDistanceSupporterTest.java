package overlay_matrix_graph.supporters;

import junit.framework.TestCase;
import objects.Point;
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
import java.util.stream.Collectors;

public class HammingDistanceSupporterTest extends TestCase {

    private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";

    public void testCorrectnessOfHammingSupporter() {
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
        HammingDistanceSupporter hamming = null;
        try {
            hamming = new HammingDistanceSupporter(points, 1);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        LinearSupporter linear = new LinearSupporter(points);
        List<Point> pointsToBeTested = Util.GenerateRandomPoints(100, 36, 46, 7, 18);
        long time = System.nanoTime();
        for(Point pointToBeTested : pointsToBeTested) {
            List<Point> linearResult = linear.searchNeighbours(pointToBeTested, 4).stream().map(NeighbourResponse::getPoint).collect(Collectors.toList());
            System.out.println("Linear research requires:  " + (System.nanoTime() - time) / 1000 + " micros");
            time = System.nanoTime();
            List<Point> hammingResult = hamming.searchNearestBucket(pointToBeTested);
            System.out.println("Hamming research requires:  " + (System.nanoTime() - time) / 1000 + " micros");

            int count = 0;
            for (Point linearPoint : linearResult)
                for (Point hammingPoint : hammingResult)
                    if (linearPoint.sameCoordinates(hammingPoint))
                        count++;
            System.out.println("Point to be searched: " + pointToBeTested);
            if (count == 0) {
                System.err.println("No intersection between result");
                System.out.println("Linear result: ");
                System.out.println(linearResult);
                System.out.println("Hamming result: " + hammingResult.size() + " points");
                System.out.println(hammingResult);
                fail();
            } else {
                System.out.println("Founded " + count + " good results");
            }
            System.out.println("Linear result: ");
            System.out.println(linearResult);
            System.out.println("Hamming result: " + hammingResult.size() + " points");
            System.out.println(hammingResult+ "\n\n");
        }
    }

}