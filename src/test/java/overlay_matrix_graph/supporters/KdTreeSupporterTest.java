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

public class KdTreeSupporterTest extends TestCase {
    private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\FilteredNodes\\FilteredFilteredResult.xlsx";
    private static final String PATH_TO_TEST = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\geocodedAddresses.xlsx";

    public void testKdTreeSupporterCorrectness() {
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
        KdTreeSupporter kdtree = null;
        try {
            kdtree = new KdTreeSupporter(points, true);
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
        sheet = workbook.getSheetAt(1);
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
        for(Point pointToBeTested : pointsToBeTested) {
            List<Point> linearResult = linear.searchNeighbours(pointToBeTested, 4);

            List<Point> treeResult = kdtree.searchNeighbours(pointToBeTested, 4);

            for(Point p : linearResult) {
                for(Point p1 : treeResult) {
                    if(p.sameCoordinates(p1)) {
                        treeResult.remove(p1);
                        break;
                    }

                }
            }
            if(!treeResult.isEmpty())
                fail();

        }

        long time = System.nanoTime();
        for(Point pointToBeTested : pointsToBeTested) {
            linear.searchNeighbours(pointToBeTested, 4);
        }
        System.out.println("Linear computation done in "+ (System.nanoTime()-time)/1000000 + " ms");

        time = System.nanoTime();
        for(Point pointToBeTested : pointsToBeTested) {
            kdtree.searchNeighbours(pointToBeTested, 4);
        }
        System.out.println("Tree computation done in "+ (System.nanoTime()-time)/1000000 + " ms");

    }

    public void testTreeCreation() {
        try {
            KdTreeSupporter tree = new KdTreeSupporter(Util.GenerateRandomPoints(10), true);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testKNNCorrectnessTest() {
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
        KdTreeSupporter tree = new KdTreeSupporter(points, true);
        LinearSupporter linear = new LinearSupporter(points);

        try (FileInputStream file = new FileInputStream(new File(PATH_TO_TEST)))
        {
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }
        List<Point> pointsToBeTested = new ArrayList<>();
        sheet = workbook.getSheetAt(1);
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

        for(Point point : pointsToBeTested) {
            ArrayList<Point> linearResult = new ArrayList<>(linear.searchNeighbours(point, 4));
            ArrayList<Point> treeResult = new ArrayList<>(tree.searchNeighbours(point, 4));
            System.out.println("Point : " + point);
            System.out.println("Linear result: " + linearResult);
            System.out.println("Tree result: " + treeResult);

            for(Point p : linearResult) {
                for(Point p1 : treeResult) {
                    if(p.sameCoordinates(p1)) {
                        treeResult.remove(p1);
                        break;
                    }

                }
            }
            if(!treeResult.isEmpty())
                fail();
        }
    }

    public void testKNNAngleCorrectnessTest() {
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
        KdTreeSupporter tree = new KdTreeSupporter(points, true);
        LinearSupporter linear = new LinearSupporter(points);

        try (FileInputStream file = new FileInputStream(new File(PATH_TO_TEST)))
        {
            workbook = new XSSFWorkbook(file);
        } catch(IOException e) {
            System.err.println("Test: Error opening the file");
            fail();
        }
        List<Point> pointsToBeTested = new ArrayList<>();
        sheet = workbook.getSheetAt(1);
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

        for(Point point : pointsToBeTested) {
            ArrayList<Point> linearResult = new ArrayList<>(linear.searchNeighbours(point, 4, 50.0));
            ArrayList<Point> treeResult = new ArrayList<>(tree.searchNeighbours(point, 4, 50.0));
            System.out.println("Point : " + point);
            System.out.println("Linear result: " + linearResult);
            System.out.println("Tree result: " + treeResult);

            for(Point p : linearResult) {
                for(Point p1 : treeResult) {
                    if(p.sameCoordinates(p1)) {
                        treeResult.remove(p1);
                        break;
                    }

                }
            }
            if(!treeResult.isEmpty())
                fail();
        }
    }
}