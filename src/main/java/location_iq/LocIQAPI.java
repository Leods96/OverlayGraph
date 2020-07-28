package location_iq;

import location_iq.exceptions.BadResponseException;
import location_iq.exceptions.DayRequestFinishedException;
import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class LocIQAPI {

    private static final String EUROPE_BASE_URL = "https://eu1.locationiq.com/v1";
    private static final String BALANCE = "/balance.php?";
    private static final String FORMAT = "&format=json";
    private String service = "";
    private static final String PROFILE = "/driving"; //There are no other type o profiles
    private String coordinates = "";
    private String token = "";
    private String option = "";
    //TODO remove - test cp
    private int counter = 0;


    public JSONObject request() throws IOException, BadResponseException, DayRequestFinishedException {
        URL url = new URL(EUROPE_BASE_URL + service + PROFILE + coordinates + token + option);
        return performHttpsRequest(url);
    }

    public JSONObject balance() throws IOException, BadResponseException, DayRequestFinishedException{
        URL url = new URL(EUROPE_BASE_URL + BALANCE + token + FORMAT);
        return performHttpsRequest(url);
    }

    public JSONObject requestReverse (Point point) throws IOException, BadResponseException, DayRequestFinishedException {
        StringBuilder sb = new StringBuilder();
        sb.append("https://eu1.locationiq.com/v1/reverse.php?");
        sb.append(token);
        sb.append("&lat="+point.getLatitude()+"&lon="+point.getLongitude());
        sb.append("&format=json");
        URL url = new URL(sb.toString());
        return performHttpsRequest(url);
    }

    private JSONObject performHttpsRequest(URL url) throws IOException, BadResponseException, DayRequestFinishedException{
        System.out.println(url);
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            return getResponse(connection);
        }finally {
            if(connection != null)
                connection.disconnect();
        }
    }

    private JSONObject getResponse(HttpsURLConnection connection) throws BadResponseException ,IOException, DayRequestFinishedException{
        int responseCode = connection.getResponseCode();
        if(responseCode == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String input;
            while ((input = br.readLine()) != null) {
                sb.append(input);
            }
            br.close();
            return new JSONObject(sb.toString());
        }
        if(responseCode == 429)
            throw new DayRequestFinishedException();
        throw new BadResponseException(connection.getResponseMessage(), responseCode);
    }

    public LocIQAPI setMatrixAnnotation(boolean distance, boolean duration){
        if(distance && duration)
            this.option = option + "&annotations=distance,duration";
        else if(distance)
            this.option = option + "&annotations=distance";
        else if (duration)
            this.option = option + "&annotations=duration";
        return this;
    }

    public LocIQAPI setService(String type){
        switch (type){
            case "matrix":{
                this.service = "/matrix";
                break;
            }
            case "directions":{
                this.service = "/directions";
                break;
            }
            default:{
                System.out.println("Unknown service");
            }
        }
        return this;
    }

    public LocIQAPI setMatrixAsService(){
        this.service = "/matrix";
        return this;
    }

    public LocIQAPI setDirectionsAsService(){
        this.service = "/directions";
        return this;
    }


    public LocIQAPI setCoordinates(List<Double> coor){
        StringBuilder sb = new StringBuilder();
        boolean separator = true;
        for (Double d: coor) {
            sb.append(d);
            if(separator)
                sb.append(",");
            else
                sb.append(";");
            separator = !separator;
        }
        sb.deleteCharAt(sb.lastIndexOf(";"));
        this.coordinates = "/" + sb.toString() + "?";
        return this;
    }

    public LocIQAPI setCoordinates(Point from, Point to){
        StringBuilder sb = new StringBuilder();
        sb.append(from.getLongitude() + "," + from.getLatitude() + ";" +to.getLongitude() + "," + to.getLatitude());
        this.coordinates = "/" + sb.toString() + "?";
        return this;
    }

    public LocIQAPI setToken(String token){
        this.token = "key=" + token;
        return this;
    }

    public LocIQAPI setMatrixDistanceDurationOption(boolean distance, boolean duration){
        StringBuilder sb;
        if(distance || duration) {
            sb = new StringBuilder();
            sb.append("&annotations=");
            if(distance && duration)
                sb.append("distance,duration");
            else {
                if (distance)
                    sb.append("distance");
                if (duration)
                    sb.append("duration");
            }
            option = option + sb.toString();
        }
        return this;
    }

    public LocIQAPI setDirectionOptions(boolean steps, boolean annotations){
        StringBuilder sb = new StringBuilder();
        if(steps)
            sb.append("&steps=true");
        if(annotations) {
            sb.append("&annotations=true");
        }
        option = option + sb.toString();
        return this;
    }
}
