package countryInformationAPI;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is used to preform requests to the https://chromium-i18n.appspot.com/ssl-address/data/
 * website to obtain countries' information
 */
public class CountryInfoRequest {
    private static final String HTTPS_URL = "https://chromium-i18n.appspot.com/ssl-address/data/";
    private final URL url;

    /**
     * ISO code information here https://en.wikipedia.org/wiki/List_of_ISO_3166_country_codes
     * @param countryCode is the ISO-3166 Alpha-2 code of the requested country
     * @throws MalformedURLException
     */
    CountryInfoRequest(String countryCode) throws MalformedURLException {
        url = new URL(HTTPS_URL + countryCode);
    }

    /**
     * Perform the request
     * @return JSONObject containing the data
     */
    public JSONObject executeRequest() throws IOException {
        HttpsURLConnection con = null;
        try {
            con = (HttpsURLConnection) url.openConnection();
            return getResponse(con);
        }finally {
            if(con != null)
                con.disconnect();
        }
    }

    private static JSONObject getResponse(HttpsURLConnection con) throws IOException{
        if (con != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String input;
            while ((input = br.readLine()) != null) {
                sb.append(input);
            }
            br.close();
            return new JSONObject(sb.toString());
        }
        throw new ConnectException("connection is null");
    }


}
