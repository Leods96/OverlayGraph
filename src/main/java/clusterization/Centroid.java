package clusterization;

/**
 * Encapsulates all coordinates for a particular cluster centroid.
 */
public class Centroid {

    private Double longitude;

    private Double latitude;

    public Centroid(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Centroid centroid = (Centroid) o;
        return this.latitude == centroid.getLatitude() && this.longitude == centroid.getLongitude();
    }

    @Override
    public String toString() {
        return "Centroid lat: " + latitude + " lon: " + longitude;
    }
}


