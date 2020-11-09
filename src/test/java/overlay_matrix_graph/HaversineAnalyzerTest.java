package overlay_matrix_graph;

import graph_hopper.GraphHopperInstance;
import junit.framework.TestCase;
import objects.Point;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import util.HeartDistance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class HaversineAnalyzerTest extends TestCase {
    private static final String OSM_FILE = "C:\\Users\\leo\\Desktop\\Stage\\OSM\\italy.osm.pbf";
    private static final String NAME = "TestInnerCityMilano";
    private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\"+ NAME + ".xlsx";
    private static final String PATH_TO_WRITE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test_Haversine_vs_GH\\"+ NAME + "result.xlsx";
    //private static final String PATH_TO_READ = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\"+ NAME + ".xlsx";
    //private static final String PATH_TO_WRITE = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\"+ NAME + "Haversine_Result.xlsx";

    @Test
    public void testDifferencesBetweenHaversineAndGH() {
        ArrayList<Object[]> result = new ArrayList<>();
        ArrayList<Object[]> resultInternal = new ArrayList<>();
        GraphHopperInstance gh = new GraphHopperInstance();
        gh.preprocessing(OSM_FILE);
        HeartDistance haversine = new HeartDistance();
        ArrayList<Point> nodes = new ArrayList<>();
        ArrayList<Point> internal = new ArrayList<>();
        XSSFWorkbook workbook = null;
        try(
                FileInputStream file = new FileInputStream(new File(PATH_TO_READ));
        ){
             workbook = new XSSFWorkbook(file);

        } catch(IOException e) {
            System.err.println("Error opening the file");
            fail();
        }
        //Read of the node to route
        int rowCount = 0;
        boolean differentDestination = false;
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.rowIterator();
        iterator.next();
        while(iterator.hasNext()) {
            Row row = iterator.next();
            try {
                if(row.getCell(0).getStringCellValue().equalsIgnoreCase("Internal")) {
                    differentDestination = true;
                    continue;
                }
                if(differentDestination) {
                    internal.add(new Point(row.getCell(0).getStringCellValue(),
                            row.getCell(1).getNumericCellValue(),
                            row.getCell(2).getNumericCellValue()));
                } else {
                    nodes.add(new Point(row.getCell(0).getStringCellValue(),
                            row.getCell(1).getNumericCellValue(),
                            row.getCell(2).getNumericCellValue()));
                }
            } catch (NullPointerException e) {
                break;
            }
            rowCount++;
        }

        for(int i = 0; i < nodes.size(); i++) {
            for(int j = 0; j < nodes.size(); j++) {
                if(i != j && !nodes.get(i).getCode().equalsIgnoreCase("T460") && !nodes.get(j).getCode().equalsIgnoreCase("T460")) {
                    Point origin = nodes.get(i);
                    Point destination = nodes.get(j);
                    double ghDist = gh.routing(origin, destination).getDistance();
                    double haversineDist = haversine.calculate(origin, destination);
                    //haversineDist = markFormula(haversineDist);
                    Object[] array = new Object[6];
                    array[0] = origin.getCode();
                    array[1] = destination.getCode();
                    array[2] = (int) haversineDist;
                    array[3] = (int) ghDist;
                    array[4] = (int) ghDist - (int) haversineDist;
                    if((int)array[4] < 0)
                        array[4] = (int) array[4] * -1;
                    array[5] = ghDist/haversineDist;
                    result.add(array);
                }
            }
        }
        if(!internal.isEmpty())
            for(Point n:nodes)
                for(Point i:internal) {
                    double ghDist = gh.routing(i, n).getDistance();
                    double haversineDist = haversine.calculate(i, n);
                    //haversineDist = markFormula(haversineDist);
                    Object[] array = new Object[6];
                    array[0] = i.getCode();
                    array[1] = n.getCode();
                    array[2] = (int) haversineDist;
                    array[3] = (int) ghDist;
                    array[4] = (int) ghDist - (int) haversineDist;
                    if((int)array[4] < 0)
                        array[4] = (int) array[4] * -1;
                    array[5] = ghDist/haversineDist;
                    resultInternal.add(array);
                }

        Comparator<Object[]> comp = new Comparator<Object[]>() {
            @Override
            public int compare(Object[] o1, Object[] o2) {
                return ((Integer)o1[4]).compareTo((Integer)o2[4]);
            }
        };

        Collections.sort(result, comp);
        Collections.sort(resultInternal, comp);

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
        row.createCell(7).setCellValue("Haversine");
        row.createCell(8).setCellValue("GH");
        row.createCell(10).setCellValue("Diff");
        row.createCell(11).setCellValue("Rat");

        for(int i = 0; i < result.size(); i++) {
            Object[] o = result.get(i);
            row = sheet.createRow(count);
            if ((count <= rowCount && !differentDestination) || (count <= rowCount + 1 && differentDestination)) {
                try {
                    row.createCell(0).setCellValue(nodes.get(i).getCode());
                    row.createCell(1).setCellValue(nodes.get(i).getLatitude());
                    row.createCell(2).setCellValue(nodes.get(i).getLongitude());
                } catch (IndexOutOfBoundsException e) {
                    if(i == nodes.size())
                        row.createCell(0).setCellValue("Internal");
                    else {
                        row.createCell(0).setCellValue(internal.get(i - nodes.size() - 1).getCode());
                        row.createCell(1).setCellValue(internal.get(i - nodes.size() - 1).getLatitude());
                        row.createCell(2).setCellValue(internal.get(i - nodes.size() - 1).getLongitude());
                    }

                }
            }
            count++;
            row.createCell(5).setCellValue((String)o[0]);
            row.createCell(6).setCellValue((String)o[1]);
            row.createCell(7).setCellValue((int) o[2]);
            row.createCell(8).setCellValue((int) o[3]);
            row.createCell(10).setCellValue((int) o[4]);
            row.createCell(11).setCellValue((double) o[5]);
        }
        count = count + 2;
        if(!internal.isEmpty()) {
            row = sheet.createRow(count);
            row.createCell(4).setCellValue("From internal to nodes");
            count++;
            for(int i = 0; i < resultInternal.size(); i++) {
                Object[] o = resultInternal.get(i);
                row = sheet.createRow(count);
                count++;
                row.createCell(5).setCellValue((String)o[0]);
                row.createCell(6).setCellValue((String)o[1]);
                row.createCell(7).setCellValue((int) o[2]);
                row.createCell(8).setCellValue((int) o[3]);
                row.createCell(10).setCellValue((int) o[4]);
                row.createCell(11).setCellValue((double) o[5]);
            }
        }

        ArrayList<String> formulas = new ArrayList<>();
        formulas.add("Max Rat");
        formulas.add("MAX(L2:L" + (result.size() + 1) + ")");
        formulas.add("Min Rat");
        formulas.add("MIN(L2:L" + (result.size() + 1) + ")");
        formulas.add("Avg Rat");
        formulas.add("AVERAGE(L2:L" + (result.size() + 1) + ")");

        formulas.add("Max IntRat");
        formulas.add("MAX(L"+ (result.size() + 4) +":L" + (result.size() + 4 + resultInternal.size() + 1) + ")");
        formulas.add("Min IntRat");
        formulas.add("MIN(L"+ (result.size() + 4) +":L" + (result.size() + 4 + resultInternal.size() + 1) + ")");
        formulas.add("Avg IntRat");
        formulas.add("AVERAGE(L"+ (result.size() + 4) +":L" + (result.size() + 4 + resultInternal.size() + 1) + ")");


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

        try(FileOutputStream outputStream = new FileOutputStream(PATH_TO_WRITE))
        {
            workbook.write(outputStream);
        } catch (IOException e) {
            System.err.println("Error writing the file");
            fail();
        }
        assertTrue(true);
    }

    public double markFormula(double haversineDistance) {
        //Speed in KM/H
        final int speed = 50;
        double distance_in_km = haversineDistance/1000;
        double t = distance_in_km/speed;
        t = t * 3600;

        return (t * 0.7369 * Math.log(t) + 18.202) * 4.621699117
        ;
    }
}
