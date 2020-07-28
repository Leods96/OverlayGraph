package overpass;

import junit.framework.TestCase;
import location_iq.exceptions.BadResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class OverpassAPITest extends TestCase {
    private static final String query = "[output:json];" +
            "area[name=\"Italia\"]->.a;" +
            "way(area.a)[highway=\"motorway\"]->.b;" +
            "node(w.b)[\"highway\"=\"motorway_junction\"];" +
            "out meta;";

    public void testOverpassAPI() {
        OverpassAPI api = new OverpassAPI();
        JSONObject json = null;
        try {
            json = api.request(query, true);
        } catch (IOException e) {
            System.err.println("IOException");
            e.printStackTrace();
            fail();
        } catch (BadResponseException e) {
            System.err.println("BadRespExc");
            e.printStackTrace();
            fail();
        }

        JSONArray array = (JSONArray) json.get("elements");

        for(int i=0; i < array.length(); i++) {

        }

        try (FileWriter file = new FileWriter("C:\\Users\\leo\\Desktop\\Stage\\Overpass\\HighwayExits.json")) {

            file.write(json.toString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(true);
    }
}