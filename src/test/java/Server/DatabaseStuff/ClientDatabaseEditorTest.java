package Server.DatabaseStuff;

import Server.PhysicalLocationStuff.SensorLocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientDatabaseEditorTest {

    private Database database;
    private ClientDatabaseEditor underTest;

    @Before
    public void setUpUnderTest() {
        database = new Database("ClientManagementDatabaseTest", "http://localhost:8086/");
        underTest = new ClientDatabaseEditor(database);
    }

    @After
    public void clearUp() {
        database.deleteDatabase("ClientManagementDatabaseTest");
    }

    @Test
    public void shouldBeAbleToAddSitesForClientAndGetThemOut() {
        underTest.createNewClient("c");
        underTest.addSiteForClient("c", "s1");
        underTest.addSiteForClient("c", "s2");
        List<String> clientSites = underTest.getSiteNamesForClient("c");
        assertTrue(clientSites.contains("s1"));
        assertTrue(clientSites.contains("s2"));
    }

    @Test
    public void shouldBeAbleToGetListOfClients() {
        underTest.createNewClient("client1");
        underTest.createNewClient("client2");
        List<String> clientList = underTest.getClientNames();
        assertTrue(clientList.contains("client1"));
        assertTrue(clientList.contains("client2"));
    }

    @Test
    public void shouldBeAbleToQuerySiteOnceAdded() {
        underTest.createNewClient("c");
        underTest.addSiteForClient("c", "s");
        SensorLocation sensor = new SensorLocation("IP1", 0.1, 0.2);
        SensorLocation sensor2 = new SensorLocation("IP2", 0.3, 0.4);
        underTest.addSensorToClientSite("c", "s", sensor);
        underTest.addSensorToClientSite("c", "s", sensor2);
        List<SensorLocation> sensors = underTest.getSensorsForClientSite("c", "s");

        assertTrue(sensors.contains(sensor));
        assertTrue(sensors.contains(sensor2));
        assertEquals(2, sensors.size());
    }

    @Test
    public void shouldBeAbleToGenerateConfigFile() throws IOException {
        underTest.createNewClient("c");
        underTest.addSiteForClient("c", "s");
        underTest.generateConfigFileForSite("c", "s", "src/test/java/Server/AccountManagement/csTest.config");

        assertTrue(Files.isRegularFile(Paths.get("src/test/java/Server/AccountManagement/csTest.config")));

        Files.delete(Paths.get("src/test/java/Server/AccountManagement/csTest.config"));
    }

    @Test
    public void shouldBeAbleToGetAesStringsFromDatabase() throws IOException {
        underTest.createNewClient("c");
        underTest.addSiteForClient("c", "s1");
        underTest.generateConfigFileForSite("c", "s1", "src/test/java/Server/AccountManagement/csTest1.config");
        underTest.addSiteForClient("c", "s2");
        underTest.generateConfigFileForSite("c", "s2", "src/test/java/Server/AccountManagement/csTest2.config");

        HashMap<String, String> sitesToAesStrings = underTest.getAesKeys();
        assertEquals(64, ((String) sitesToAesStrings.get("c.s1")).length());
        assertEquals(64, ((String) sitesToAesStrings.get("c.s2")).length());

        Files.delete(Paths.get("src/test/java/Server/AccountManagement/csTest1.config"));
        Files.delete(Paths.get("src/test/java/Server/AccountManagement/csTest2.config"));
    }

    @Test
    public void shouldBeAbleToAddUsersForClient() {
        underTest.createNewClient("c");

    }

}