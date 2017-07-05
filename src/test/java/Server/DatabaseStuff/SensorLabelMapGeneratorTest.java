package Server.DatabaseStuff;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SensorLabelMapGeneratorTest {

    private static Database tsDatabase;

    @BeforeClass
    public static void setUpDatabase() {
        tsDatabase = new Database("ts", "http://localhost:8086/");

        DatabaseEntry entry = new DatabaseEntry();
        entry.add("DeviceCollection", "client.site");
        entry.setTimestamp("2000-01-01 01:00:00.000");
        entry.add("IP", "IP123");
        entry.add("label1", "30");

        DatabaseEntry otherEntry = new DatabaseEntry();
        otherEntry.add("DeviceCollection", "client.site");
        otherEntry.setTimestamp("2000-01-01 01:00:01.000");
        otherEntry.add("IP", "IP456");
        otherEntry.add("label2", "40");

        tsDatabase.addEntry(entry);
        tsDatabase.addEntry(otherEntry);
    }

    @AfterClass
    public static void deleteDatabase() {
        tsDatabase.deleteDatabase("ts");
    }

    @Test
    public void shouldTakeSensorIpAndGetLabelsFromDatabase() {
        SensorLabelMapGenerator underTest = new SensorLabelMapGenerator(tsDatabase);
        List<String> labelsForSensor1 = underTest.getLabels("IP123", "client", "site");
        List<String> labelsForSensor2 = underTest.getLabels("IP456", "client", "site");

        assertEquals("label1", labelsForSensor1.get(0));
        assertEquals("label2", labelsForSensor2.get(0));
    }

    @Test
    public void shouldTakeListOfIPsAndProduceMapOfIpToListOfLabels() {
        SensorLabelMapGenerator underTest = new SensorLabelMapGenerator(tsDatabase);
        List<String> listIP = new ArrayList<>(Arrays.asList("IP123", "IP456"));
        Map<String, List<String>> labelMap = underTest.getLabelMap(listIP, "client", "site");
        assertEquals(Arrays.asList("label1"), labelMap.get("IP123"));
        assertEquals(Arrays.asList("label2"), labelMap.get("IP456"));
    }

    @Test
    public void shouldTakeListOfIPsAndProduceListOfLabelsForSite() {
        SensorLabelMapGenerator underTest = new SensorLabelMapGenerator(tsDatabase);
        List<String> listIP = new ArrayList<>(Arrays.asList("IP123", "IP456"));
        Set<String> labelsForSite = underTest.getLabelsForSite(listIP, "client", "site");
        assertTrue(labelsForSite.contains("label1"));
        assertTrue(labelsForSite.contains("label2"));
    }

}