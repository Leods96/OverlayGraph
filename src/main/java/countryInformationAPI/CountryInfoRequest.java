package countryInformationAPI;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;

public class CountryInfoRequest {
    private static final String HTTPS_URL = "https://chromium-i18n.appspot.com/ssl-address/data/";
    private URL url;

    CountryInfoRequest(String countryCode) throws MalformedURLException {
        url = new URL(HTTPS_URL + countryCode);
    }

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
