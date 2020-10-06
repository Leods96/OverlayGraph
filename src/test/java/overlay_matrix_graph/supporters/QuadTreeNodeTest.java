package overlay_matrix_graph.supporters;

import junit.framework.TestCase;
import location_iq.Point;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import util.EuclideanDistance;
import util.HeartDistance;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class QuadTreeNodeTest extends TestCase {
    private static final int NUMBER_OF_POINTS = 4;
    private static final List<Point> points = new ArrayList<>();
    private QuadTreeNode tree;
    private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private static final String PATH_TO_TEST = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";


    @Before
    public void treeCreation() {
        points.addAll(Util.GenerateRandomPoints(NUMBER_OF_POINTS));
        tree = new QuadTreeNode(points);
    }

    @Test
    public void testTreeSearchCorrectnessAndTime() {
        treeCreation();
        tree.printTree();
        Point pointToSearch = Util.GenerateRandomPoints(1).get(0);
        System.out.println("\nPoint to be searched: " + pointToSearch);
        double standardTime = System.nanoTime();
        Point standardSerachPoint = standardSearch(pointToSearch, points);
        standardTime = System.nanoTime() - standardTime;
        double treeTime = System.nanoTime();
        Point treeSearchPoint = treeSearch(pointToSearch, tree).get(0);
        treeTime = System.nanoTime() - treeTime;
        assertEquals(treeSearchPoint, standardSerachPoint);
        assertTrue(treeTime < standardTime);
        System.out.println("Speedup of: " + new Double(treeTime/standardTime * 100).shortValue() + "% over " + NUMBER_OF_POINTS + " points");
    }

    public Point standardSearch(Point p, List<Point> points) {
        double minDist = Double.MAX_VALUE;
        Point toBeRet = null;
        for(Point ps : points) {
            double temp = new EuclideanDistance().calculate(ps, p);
            if (temp < minDist) {
                toBeRet = ps;
                minDist = temp;
            }
        }
        return toBeRet;
    }

    public List<Point> treeSearch(Point p, QuadTreeNode tree){
        return tree.searchNeighbour(p);
    }

    public void mapTest() {
        HashMap<Point, List<Point>> h = new HashMap();
    }

    public void testOrderIntoTheLeaf() {
        treeCreation();
        tree.printTree();
        Point pointToSearch = Util.GenerateRandomPoints(1).get(0);
        ArrayList<Point> treeSearchPoint = new ArrayList<>(treeSearch(pointToSearch, tree));
        HeartDistance calculator = new HeartDistance();
        for(int i = 0; i < treeSearchPoint.size() - 1; i++)
            if(calculator.calculate(treeSearchPoint.get(i),pointToSearch) >
                    calculator.calculate(treeSearchPoint.get(i+1),pointToSearch))
                fail();
    }

    public void testCorrectnessOfQuadTree() {
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
        QuadTreeNode tree = null;
        try {
            tree = new QuadTreeNode(points);
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
            List<Point> linearResult = linear.searchNeighbours(pointToBeTested, 4).stream().map(NeighbourResponse::getPoint).collect(Collectors.toList());
            time = System.nanoTime();
            List<Point> treeResult = tree.searchNeighbour(pointToBeTested);

            int count = 0;
            for (Point linearPoint : linearResult)
                for (Point treePoint : treeResult)
                    if (linearPoint.sameCoordinates(treePoint))
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
            System.out.println("Tree result: " + treeResult.size() + " points");
            System.out.println(treeResult + "\n\n");
        }
        System.out.println("Failed intersection : " + failedIntersection);
    }

}