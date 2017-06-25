package Server.PhysicalLocationStuff;

public class SensorLocation {
    private static final int RADIUS_OF_WORLD = 3961;

    private String name;
    private double latitude;
    private double longitude;

    public SensorLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double distanceTo(SensorLocation second) {
        double deltaLat = Math.toRadians(latitude - second.latitude);
        double deltaLong = Math.toRadians(longitude - second.longitude);
        double mathsThing = Math.sin(deltaLat/2)*Math.sin(deltaLat/2) + Math.cos(Math.toRadians(latitude))
                *Math.cos(Math.toRadians(second.latitude))
                *Math.sin(deltaLong/2)*Math.sin(deltaLong/2);
        double c = 2*Math.atan2(Math.sqrt(mathsThing),Math.sqrt(1 - mathsThing));
        return RADIUS_OF_WORLD*c;
    }

    public boolean equals(Object other) {
        SensorLocation otherLocation = (SensorLocation) other;
        return otherLocation.getLatitude() == getLatitude()
                && otherLocation.getLongitude() == getLongitude();
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
