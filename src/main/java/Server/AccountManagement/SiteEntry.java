package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DatabaseEntrySet;
import Server.DatabaseStuff.DeviceCollection;
import Server.PhysicalLocationStuff.SensorLocation;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static Server.DatabaseStuff.DatabaseEntry.timestampFormat;

public class SiteEntry {
    private DeviceCollection siteDetails;
    private HashMap<String, SensorLocation> sensorLocations; // hashmap of IPv6 address to lat and long. Will find smart way to store as string in db
    private int sensorCount = 1;

    public SiteEntry(String clientName, String siteName) {
        siteDetails = new DeviceCollection(clientName, siteName);
        sensorLocations = new HashMap<>();
    }

    public void addSensorDetails(String ipAddress, double lat, double lon) {
        sensorLocations.put(ipAddress, new SensorLocation(ipAddress, lat, lon));
    }

    public void addSensor(SensorLocation sensor) {
        sensorLocations.put(sensor.getName(), sensor);
    }

    public DatabaseEntrySet getDbEntries() {
        DatabaseEntrySet sensorEntries = new DatabaseEntrySet();
        sensorCount = 1;
        for (String ip : sensorLocations.keySet()) {
            SensorLocation sensor = sensorLocations.get(ip);
            DatabaseEntry entry = generateDatabaseEntryFromSensorLocation(ip, sensor);
            sensorEntries.add(entry);
        }
        return sensorEntries;
    }

    private DatabaseEntry generateDatabaseEntryFromSensorLocation(String ip, SensorLocation sensor) {
        DatabaseEntry entry = new DatabaseEntry();
        entry.setTimestamp(timestampFormat.format(new Date(1000*sensorCount++)));
        entry.setDeviceCollectionIdentifier(siteDetails);
        entry.add("lat", sensor.getLatitude());
        entry.add("lon", sensor.getLongitude());
        entry.add("IP", ip);
        return entry;
    }

    public static SiteEntry getSiteFromDbEntrySet(DatabaseEntrySet entrySet) {
        SiteEntry result = generateSiteEntryWithName(entrySet);
        for (int i = 0; i < entrySet.size(); i++) {
            DatabaseEntry currentEntry = entrySet.get(i);
            addEntryToSite(currentEntry, result);
        }
        return result;
    }

    private static SiteEntry generateSiteEntryWithName(DatabaseEntrySet entrySet) {
        String[] clientSitePair = entrySet.get(0).getDeviceCollectionIdentifier().split("\\.");
        return new SiteEntry(clientSitePair[0], clientSitePair[1]);
    }

    private static void addEntryToSite(DatabaseEntry currentEntry, SiteEntry result) {
        String ip = (String) currentEntry.get("IP");
        double lat = (double) currentEntry.get("lat");
        double lon = (double) currentEntry.get("lon");
        SensorLocation currentLocation = new SensorLocation(ip, lat, lon);
        result.sensorLocations.put(ip, currentLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteEntry siteEntry = (SiteEntry) o;

        if (!siteDetails.equals(siteEntry.siteDetails)) return false;
        return sensorLocations.equals(siteEntry.sensorLocations);
    }

    @Override
    public int hashCode() {
        int result = siteDetails.hashCode();
        result = 31 * result + sensorLocations.hashCode();
        return result;
    }

    public DatabaseEntry getDbEntryForSensor(SensorLocation sensor) {
        sensorLocations.put(sensor.getName(), sensor);
        sensorCount = sensorLocations.size();
        return generateDatabaseEntryFromSensorLocation(sensor.getName(), sensor);
    }

    public List<SensorLocation> getArrayOfSensors() {
        List<SensorLocation> result = new ArrayList<>();
        for (String ip : sensorLocations.keySet()) {
            result.add(sensorLocations.get(ip));
        }
        return result;
    }
}
