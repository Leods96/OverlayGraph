package objects;

import java.io.Serializable;

/**
 * Object that represents a Geocoded point over a map
 * Each point has a unique code that work as identifier and in represented as combination of two data:
 * Latitude and Longitude representing the position on the earth map
 */
public class Point implements Serializable {

    private String code;
    private final Double latitude;
    private final Double longitude;

    /**
     * standard Constructor into which all the parameters are defined
     */
    public Point(String code, Double latitude, Double longitude){
        this.code = code;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor with only coordinates, used for test of for a temporal instance of the object
     */
    public Point(Double latitude, Double longitude){
        this.code = null;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setCode(String code) {
        this.code = code;
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
        return this.latitude.equals(p.getLatitude()) && this.longitude.equals(p.getLongitude()) && this.code.equals(p.getCode());
    }

    public boolean sameCoordinates(Point p) {
        return this.latitude.equals(p.getLatitude()) && this.longitude.equals(p.getLongitude());
    }

    @Override
    public String toString() {
        return (code == null ? "" : " Code: " + code) + " Lat: " + latitude + " Lon: " + longitude;
    }

}
