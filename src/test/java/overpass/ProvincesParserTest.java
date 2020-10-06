package overpass;

import junit.framework.TestCase;
import location_iq.Point;
import util.BoundaryNodesFilter;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ProvincesParserTest extends TestCase {

    private static final String COUNTRY_INFO_DIR = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation";
    private static final String NODES_DIR = COUNTRY_INFO_DIR + "\\Nodes";

    public void testProvincesParser() {
        ProvincesParser parser = new ProvincesParser(COUNTRY_INFO_DIR);
        try {
            parser.process(true);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testNumberOfNodesInProvinceBoundaries() {
        try {
            System.out.println("Number of nodes: " + BoundaryNodesFilter.countNodes(COUNTRY_INFO_DIR + "\\Nodes"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testFilterNodesOnDistance() {
        try {
            BoundaryNodesFilter.boundaryNodesFilterOnDistance(COUNTRY_INFO_DIR);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testFilterNodesOnBoundaries() {
        try {
            BoundaryNodesFilter.boundaryNodesFilterOnBoundary(COUNTRY_INFO_DIR);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testGetQueryForDistribution() {
        try {
            System.out.println(BoundaryNodesFilter.produceStringForRequest(COUNTRY_INFO_DIR + "\\FilteredNodes"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testConversion() {
        double d = 44.99345;
        int latVal = (int) d;
        System.out.println(latVal);
    }

    public void testDegreese() {
        Point a = new Point(41.9, 13.4);
        Point b = new Point (43.3, 12.6);
        System.out.println("Angle: " + angle(a, b));
    }


    private double angle(Point start, Point end) {
        double lon1 = start.getLongitude();
        double lat1 = start.getLatitude();
        double lon2 = end.getLongitude();
        double lat2 = end.getLatitude();

        double dLon = Math.toRadians(lon2-lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2) -
                Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));

        // fix negative degrees
        if(brng<0) {
            brng=360-Math.abs(brng);
        }

        return brng;
    }


    public void testArrayFIlter() {
        ArrayList<Integer> array = new ArrayList<>();
        array.add(1);
        array.add(2);
        array.add(3);
        array.add(4);
        array.add(5);
        array.stream().filter(i -> i < 3).collect(Collectors.toList());
        System.out.println(array);
    }

}