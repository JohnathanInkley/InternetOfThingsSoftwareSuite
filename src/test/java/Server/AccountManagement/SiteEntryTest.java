package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DatabaseEntrySet;
import Server.PhysicalLocationStuff.SensorLocation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SiteEntryTest {

    @Test
    public void shouldBeAbleToMakeDBEntryFromSiteEntry() {
        SiteEntry underTest = new SiteEntry("client", "site");
        underTest.addAesString("keyString");
        underTest.addSensorDetails("IP1", 0.1, 0.2);
        underTest.addSensorDetails("IP2", 0.3, 0.4);

        DatabaseEntrySet sensorEntries = underTest.getDbEntries(); // we should add a site as a table into our site database: can't imagine we'll have bazillions of sites

        assertEquals("1970-01-01 00:00:01.000", sensorEntries.get(0).getTimestamp());
        assertEquals("1970-01-01 00:00:02.000", sensorEntries.get(1).getTimestamp());
        assertEquals("client.site", sensorEntries.get(0).getDeviceCollectionIdentifier());
        assertEquals("keyString", sensorEntries.get(0).get("aesString"));
        assertEquals("IP2", sensorEntries.get(0).get("IP"));
        assertEquals(0.3, (Double) sensorEntries.get(0).get("lat"),0.00001);
        assertEquals(0.4, (Double) sensorEntries.get(0).get("lon"),0.00001);
    }

    @Test
    public void shouldBeAbleToMakeSiteEntryFromDatabaseEntrySet() {
        DatabaseEntrySet entrySet = new DatabaseEntrySet();
        DatabaseEntry entry = new DatabaseEntry();
        entry.add("DeviceCollection", "client.site");
        entry.setTimestamp("1970-01-01 01:00:01.000");
        entry.add("lat", 0.1);
        entry.add("lon", 0.2);
        entry.add("IP", "IP1");
        entry.add("aesString", "keyString");
        entrySet.add(entry);
        SiteEntry underTest = SiteEntry.getSiteFromDbEntrySet(entrySet);

        SiteEntry expectedSite = new SiteEntry("client", "site");
        expectedSite.addSensorDetails("IP1", 0.1, 0.2);
        expectedSite.addAesString("keyString");

        assertEquals(expectedSite, underTest);
    }

    @Test
    public void shouldBeAbleToMakeDbEntryForSensor() {
        SiteEntry underTest = new SiteEntry("client", "site");
        underTest.addAesString("keyString");
        SensorLocation sensor = new SensorLocation("IP1", 0.1, 0.2);
        DatabaseEntry actualEntry = underTest.getDbEntryForSensor(sensor);

        DatabaseEntry expectedEntry = new DatabaseEntry();
        expectedEntry.add("DeviceCollection", "client.site");
        expectedEntry.setTimestamp("1970-01-01 00:00:01.000");
        expectedEntry.add("lat", 0.1);
        expectedEntry.add("lon", 0.2);
        expectedEntry.add("IP", "IP1");
        expectedEntry.add("aesString", "keyString");

        assertEquals(expectedEntry, actualEntry);
    }

    @Test
    public void shouldMakeAesEntryIfNoSensorsAdded() {
        SiteEntry underTest = new SiteEntry("client", "site");
        underTest.addAesString("keyString");
        DatabaseEntrySet dbEntries = underTest.getDbEntries();

        assertEquals(1, dbEntries.size());
        assertEquals("aesEntry", dbEntries.get(0).get("IP"));

        SiteEntry second = SiteEntry.getSiteFromDbEntrySet(dbEntries);
        assertEquals(dbEntries, second.getDbEntries());
    }

}