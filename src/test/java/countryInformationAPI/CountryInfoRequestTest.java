package countryInformationAPI;

import junit.framework.TestCase;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class CountryInfoRequestTest extends TestCase {
    private static final String PATH_TO_SAVE_INFO = "C:\\Users\\leo\\Desktop\\ThesisProject1.0\\Addresses\\CountryInformation\\";
    private static final String FILE_NAME = "ItalyInfo.json";
    private static final String countryCode = "IT";

    public void testCountryInfoRequest() {
        JSONObject response = null;
        try {
            CountryInfoRequest apiRequest = new CountryInfoRequest(countryCode);
            response = apiRequest.executeRequest();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        try (FileWriter file = new FileWriter(PATH_TO_SAVE_INFO + FILE_NAME)) {
            file.write(response.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}