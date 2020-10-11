package external_api;

import junit.framework.TestCase;
import external_api.exceptions.BadResponseException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class OverpassAPITest extends TestCase {
    private static final String queryHighway = "[output:json];" +
            "area[name=\"Italia\"]->.a;" +
            "way(area.a)[highway=\"motorway\"]->.b;" +
            "node(w.b)[\"highway\"=\"motorway_junction\"];" +
            "out meta;";

    private static final String queryBoundary = "[output:json];" +
            "area[name=\"Lombardia\"]->.a;" +
            "relation(area.a)[type=\"boundary\"][\"ISO3166-2\"][\"admin_level\"=\"6\"][\"boundary\"=\"administrative\"]->.boundary;" +
            "way(r.boundary)->.boundary;" +
            "way(area.a)[highway][highway~\"trunk|primary|secondary|trunk_link|primary_link|secondary_link\"]->.highway;" +
            "node(w.highway)(around.boundary:1);" +
            "out meta;";

    public void testOverpassAPI() {
        OverpassAPI api = new OverpassAPI();
        JSONObject json = null;
        try {
            json = api.request(queryBoundary, true);
        } catch (IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
            fail();
        } catch (BadResponseException e) {
            System.err.println("BadRespExc");
            e.printStackTrace();
            fail();
        }

        try (FileWriter file = new FileWriter("C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\Test\\ProvinceBoundaries\\LombardiaBoundaries.json")) {
            file.write(json.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}