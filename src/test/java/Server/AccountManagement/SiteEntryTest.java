package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntrySet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SiteEntryTest {

    @Test
    public void shouldBeAbleToMakeDBEntryFromSiteEntry() {
        SiteEntry underTest = new SiteEntry("client", "site");
        underTest.addSensorDetails("IP1", 0.1, 0.2);
        underTest.addSensorDetails("IP2", 0.3, 0.4);

        DatabaseEntrySet sensorEntries = underTest.getDbEntries(); // we should add a site as a table into our site database: can't imagine we'll have bazillions of sites

        assertEquals("1970-01-01 01:00:01.000", sensorEntries.get(0).getTimestamp());
        assertEquals("1970-01-01 01:00:02.000", sensorEntries.get(1).getTimestamp());
        assertEquals("client.site", sensorEntries.get(0).getDeviceCollectionIdentifier());
        assertEquals("IP2", sensorEntries.get(0).get("IP"));
        assertEquals(0.3, (Double) sensorEntries.get(0).get("lat"),0.00001);
        assertEquals(0.4, (Double) sensorEntries.get(0).get("lon"),0.00001);
    }

    @Test
    public void shouldBeAbleToMakeSiteEntryFromDatabaseEntrySet() {

    }

}