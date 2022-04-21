package tourGuide.inputEntities;

public class Location {
    public double longitude;
    public double latitude;

    public Location(){}

    public Location(gpsUtil.location.Location location) {
        this.latitude = location.latitude;
        this.longitude = location.longitude;
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
