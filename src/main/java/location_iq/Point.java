package location_iq;

import java.io.Serializable;

public class Point implements Serializable {

    private String code;
    private Double latitude;
    private Double longitude;

    public Point(String code, Double latitude, Double longitude){
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(Double latitude, Double longitude){
        this.code = null;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point p = (Point) o;
        return this.latitude == p.getLatitude() && this.longitude == p.getLongitude() && this.code == p.getCode();
    }

    @Override
    public String toString() {
        return (code == null ? "" : " Code: " + code) + " Lat: " + latitude + " Lon: " + longitude;
    }

}
