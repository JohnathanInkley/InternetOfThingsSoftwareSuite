package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DatabaseEntrySet;
import Server.DatabaseStuff.DeviceCollection;
import Server.PhysicalLocationStuff.SensorLocation;

import java.util.Date;
import java.util.HashMap;

import static Server.DatabaseStuff.DatabaseEntry.timestampFormat;

public class SiteEntry {
    private DeviceCollection siteDetails;
    private HashMap<String, SensorLocation> sensorLocations; // hashmap of IPv6 address to lat and long. Will find smart way to store as string in db

    public SiteEntry(String clientName, String siteName) {
        siteDetails = new DeviceCollection(clientName, siteName);
        sensorLocations = new HashMap<>();
    }

    public void addSensorDetails(String ipAddress, double lat, double lon) {
        sensorLocations.put(ipAddress, new SensorLocation(ipAddress, lat, lon));
    }

    public DatabaseEntrySet getDbEntries() {
        DatabaseEntrySet sensorEntries = new DatabaseEntrySet();
        int sensorCount = 1;
        for (String ip : sensorLocations.keySet()) {
            SensorLocation sensor = sensorLocations.get(ip);
            DatabaseEntry entry = new DatabaseEntry();
            entry.setTimestamp(timestampFormat.format(new Date(1000*sensorCount++)));
            entry.setDeviceCollectionIdentifier(siteDetails);
            entry.add("lat", sensor.getLatitude());
            entry.add("lon", sensor.getLongitude());
            entry.add("IP", ip);
            sensorEntries.add(entry);
        }
        return sensorEntries;
    }
}
