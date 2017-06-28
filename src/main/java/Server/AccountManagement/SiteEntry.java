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
    public final static String AES_STRING_ENTRY_LABEL = "aesEntry";
    private final DeviceCollection siteDetails;
    private final HashMap<String, SensorLocation> sensorLocations;
    private String aesString;
    private int sensorCount = 1;

    public SiteEntry(String clientName, String siteName) {
        siteDetails = new DeviceCollection(clientName, siteName);
        sensorLocations = new HashMap<>();
    }

    public void addSensorDetails(String ipAddress, double lat, double lon) {
        sensorLocations.put(ipAddress, new SensorLocation(ipAddress, lat, lon));
    }

    public void addAesString(String aesString) {
        this.aesString = aesString;
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
        if (sensorEntries.isEmpty()) {
            sensorEntries.add(generateFakeEntryContainingAESKey());
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
        entry.add("aesString", aesString);
        return entry;
    }

    private DatabaseEntry generateFakeEntryContainingAESKey() {
        return generateDatabaseEntryFromSensorLocation(AES_STRING_ENTRY_LABEL,
                new SensorLocation(aesString,0,0));
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
        SiteEntry result = new SiteEntry(clientSitePair[0], clientSitePair[1]);
        result.addAesString((String) entrySet.get(0).get("aesString"));
        return result;
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
        return sensorLocations.equals(siteEntry.sensorLocations) && aesString.equals(siteEntry.aesString);
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
            if (!ip.startsWith(AES_STRING_ENTRY_LABEL)) {
                result.add(sensorLocations.get(ip));
            }
        }
        return result;
    }
}
