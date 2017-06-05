package Server.DatabaseStuff;


import BaseStation.*;
import Server.BaseStationServerStuff.BaseStationConnectionServer;
import Server.BaseStationServerStuff.SensorReadingParser;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DatabaseTest {

    private String generateDatabaseNameFromCurrentTime() {
        return "db" + System.currentTimeMillis();
    }

    @Test
    public void shouldBeAbleToQueryEntriesBetweenTwoTimesUsingLongTimes() throws ParseException {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site = new DeviceCollection("owner", "factory1");
        DatabaseEntry sampleEntry = generateSampleEntry("2000-01-23 00:34:56.789", 30d);
        sampleEntry.setDeviceCollectionIdentifier(site);
        underTest.addEntry(sampleEntry);

        DatabaseEntrySet entriesInTimeFrame = underTest.getSiteEntriesBetween(site, 948587696000l, 948587697000l);
        assertEquals(1, entriesInTimeFrame.size());
        assertEquals(entriesInTimeFrame.get(0), sampleEntry);

        underTest.deleteDatabase(name);
    }

    @Test
    public void shouldBeAbleToQueryEntriesBetweenTwoTimesUsingDates() {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site = new DeviceCollection("owner", "factory1");
        DatabaseEntry sampleEntry = generateSampleEntry("2000-01-23 00:34:56.789", 30d);
        sampleEntry.setDeviceCollectionIdentifier(site);
        underTest.addEntry(sampleEntry);

        DatabaseEntrySet entriesInTimeFrame = underTest.getSiteEntriesBetween(site, "2000-01-23 00:34:55.000", "2000-01-23 00:34:58.000");
        assertEquals(1, entriesInTimeFrame.size());
        assertEquals(entriesInTimeFrame.get(0), sampleEntry);

        underTest.deleteDatabase(name);
    }

    private DatabaseEntry generateSampleEntry(String time, Double temperature) {
        DatabaseEntry sampleEntry = new DatabaseEntry();
        sampleEntry.add("temperature", temperature);
        sampleEntry.setTimestamp(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sampleEntry.setTimestampFormat(format);
        return sampleEntry;
    }

    @Test
    public void shouldBeAbleToAddMultipleDataPointsOverTimeForSameDevice() {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site = new DeviceCollection("owner", "factory1");

        for (int i = 0; i < 10; i ++) {
            String timeOfEntry = "2000-01-23 00:34:0" + i + ".000"      ;
            Double temperature = (double) (30 + i);
            DatabaseEntry entry = generateSampleEntry(timeOfEntry, temperature);
            entry.setDeviceCollectionIdentifier(site);
            underTest.addEntry(entry);
        }

        DatabaseEntrySet entriesInTimeFrame = underTest.getSiteEntriesBetween(site, "2000-01-23 00:33:59.000", "2000-01-23 00:34:11.000");
        assertEquals(10, entriesInTimeFrame.size());

        underTest.deleteDatabase(name);
    }

    @Test
    public void shouldBeAbleToAddDataForMultipleLocationsAndGetMeanForEach() {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site1 = new DeviceCollection("owner", "factory1");
        DeviceCollection site2 = new DeviceCollection("owner", "factory2");

        for (int i = 0; i < 10; i ++) {
            String timeOfEntry = "2000-01-23 00:34:0" + i + ".000"      ;
            Double temperature1 = (double) (30 + i);
            Double temperature2 = (double) (40 + i);
            DatabaseEntry entry1 = generateSampleEntry(timeOfEntry, temperature1);
            DatabaseEntry entry2 = generateSampleEntry(timeOfEntry, temperature2);
            entry1.setDeviceCollectionIdentifier(site1);
            entry2.setDeviceCollectionIdentifier(site2);
            underTest.addEntry(entry1);
            underTest.addEntry(entry2);
        }

        assertEquals(34.5, underTest.getMeanSiteEntriesForFieldBetween(site1, "temperature", "2000-01-23 00:33:59.000", "2000-01-23 00:34:11.000"), 0.00001);
        assertEquals(44.5, underTest.getMeanSiteEntriesForFieldBetween(site2, "temperature", "2000-01-23 00:33:59.000", "2000-01-23 00:34:11.000"), 0.00001);

        underTest.deleteDatabase(name);
    }

    @Test
    public void shouldReturnEmptyEntrySetIfNoDataExistsBetweenTwoDates() {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site = new DeviceCollection("owner", "factory");
        DatabaseEntrySet emptySet = underTest.getSiteEntriesBetween(site, "2000-01-23 00:33:59.000", "2000-01-23 00:34:11.000");
        assertEquals(0, emptySet.size());
    }

    @Test
    public void shouldThrowExceptionWhenMeanOfNoDataIsCalled() {
        String name = generateDatabaseNameFromCurrentTime();
        Database underTest = new Database(name, "http://localhost:8086/");
        DeviceCollection site = new DeviceCollection("owner", "factory");
        try {
            Double emptySet = underTest.getMeanSiteEntriesForFieldBetween(site, "temperature", "2000-01-23 00:33:59.000", "2000-01-23 00:34:11.000");
            fail("No data exists in region so exception should be thrown");
        } catch (Exception e) {
        }
    }

    @Test
    public void shouldThrowExceptionWhenDatabaseNotFound() {
        try {
            Database underTest = new Database("blah", "http://localhost:8087/");
            fail("No database here so should throw exception");
        } catch (Exception e) {
            assertEquals("Could not connect to given url", e.getLocalizedMessage());
        }
    }

    @Test
    public void runningWholeSetUpShouldPutEntriesIntoDatabase() throws InterruptedException {
        // Set up client side
        BaseStationManager manager = new BaseStationManager();
        SensorReadingProducer readingProducer = new SensorReadingProducer();
        readingProducer.findPortAndOpen();
        SensorReadingHandler readingHandler = new SensorReadingHandler();
        SensorReadingBackupCreator backupCreator = new SensorReadingBackupCreator("dbBackup");
        SensorReadingSender readingSender = new SensorReadingSender("http://localhost:8080/SensorServer/server");
        ReadingEncryptor encryptor = new ReadingEncryptor();
        encryptor.readKey("src/main/java/BaseStation/AESKey.txt");
        manager.setConsumer(readingSender);
        manager.setHandler(readingHandler, 10);
        manager.setProducer(readingProducer);
        manager.setBackupCreator(backupCreator);
        manager.setEncryptor(encryptor);

        // Set up client connection server
        BaseStationConnectionServer server = new BaseStationConnectionServer("http://localhost:8080/SensorServer");
        SensorReadingParser parser = new SensorReadingParser();
        server.setReadingParser(parser);
        server.setEncryptor(encryptor);

        // Create database
        Database underTest = new Database("dbFullTest", "http://localhost:8086/");
        server.setDatabase(underTest);

        // run server and client side code
        server.runServer();
        manager.start();

        // wait so entries can be added
        Thread.sleep(20000);

        // shut down
        manager.join();
        server.stopServer();
    }

}