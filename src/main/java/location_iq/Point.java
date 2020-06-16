package location_iq;

public class Point {

    private String code;
    private Double latitude;
    private Double longitude;

    public Point(String code, Double latitude, Double longitude){
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getCode() {
        return code;
    }

}
