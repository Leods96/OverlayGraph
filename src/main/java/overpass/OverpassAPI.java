package overpass;

import location_iq.exceptions.BadResponseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OverpassAPI {
    private static final String BASE_URL = "http://overpass-api.de/api/interpreter?data=";
    boolean filterResponse = false;

    public JSONObject request(String query, boolean filterResponse) throws IOException, BadResponseException {
        this.filterResponse = filterResponse;
        URL url = new URL(BASE_URL + encodeValue(query));
        return performHttpRequest(url);
    }

    private JSONObject performHttpRequest(URL url) throws IOException, BadResponseException {
        System.out.println(url);
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(0);
            connection.setReadTimeout(0);
            return getResponse(connection);
        } finally {
            if(connection != null)
                connection.disconnect();
        }
    }

    private JSONObject getResponse(HttpURLConnection connection) throws BadResponseException ,IOException {
        int responseCode = connection.getResponseCode();
        if(responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String input;
            while ((input = br.readLine()) != null) {
                sb.append(input);
            }
            br.close();
            return (filterResponse) ? filteredResponse(sb.toString()) : new JSONObject(sb.toString());
        }
        System.out.println("Response code = " + responseCode);
        System.out.println("Message: " + connection.getResponseMessage());
        System.out.println("Message: " + connection.getErrorStream());
        throw new BadResponseException(connection.getResponseMessage(), responseCode);
    }

    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
    //TODO: check if correct
    private JSONObject filteredResponse (String response) {
        JSONArray array = new JSONObject(response).getJSONArray("elements");
        JSONArray newArray = new JSONArray();
        int length = array.length();
        for(int i = 0; i < length; i++) {
            JSONObject elem = (JSONObject) array.remove(0);
            JSONObject newElem = new JSONObject();
            newElem.put("lat",elem.get("lat"));
            newElem.put("lon",elem.get("lon"));
            newElem.put("id",elem.get("id"));
            newArray.put(newElem);
        }
        return new JSONObject().put("elements",newArray);
    }
}
