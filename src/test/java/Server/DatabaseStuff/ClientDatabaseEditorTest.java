package Server.DatabaseStuff;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
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
import static org.junit.Assert.assertFalse;
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

        HashMap<String, String> sitesToAesStrings = underTest.getAesKeysForSites();
        assertEquals(64, ((String) sitesToAesStrings.get("c.s1")).length());
        assertEquals(64, ((String) sitesToAesStrings.get("c.s2")).length());

        Files.delete(Paths.get("src/test/java/Server/AccountManagement/csTest1.config"));
        Files.delete(Paths.get("src/test/java/Server/AccountManagement/csTest2.config"));
    }

    @Test
    public void shouldBeAbleToAddUsersForClient() throws IOException {
        underTest.createNewClient("c");
        String outputFile = "src/test/java/Server/AccountManagement/clientNewUserTest.txt";
        underTest.generateNewUsersForClient("c", 2, outputFile);
        UsernamePasswordPair pair = getCredentialsFromFile(outputFile);

        UserEntry user = underTest.getUserEntry(pair.username);
        assertTrue(user.validateCredentials(pair.username, pair.password));
    }

    private UsernamePasswordPair getCredentialsFromFile(String outputFile) throws IOException {
        List<String> users = Files.readAllLines(Paths.get(outputFile));
        Files.delete(Paths.get(outputFile));
        String[] usernamePassword = users.get(0).replace("username: ", "").replace("password: ", "").split("\\s+");
        return new UsernamePasswordPair(usernamePassword[0], usernamePassword[1]);
    }

    @Test
    public void onceUsersCreatedShouldBeAbleToChangeDetails() throws IOException {
        underTest.createNewClient("c");
        String outputFile = "src/test/java/Server/AccountManagement/clientNewUserTest1.txt";
        underTest.generateNewUsersForClient("c", 1, outputFile);
        UsernamePasswordPair pair = getCredentialsFromFile(outputFile);

        UserEntry user = underTest.getUserEntry(pair.username);
        user.setUserName("u");
        user.setPasswordAndHash("p");
        user.setEmail("e@gmail.com");
        user.setFirstName("f");
        user.setLastName("l");
        underTest.addUserEntry(user);

        UserEntry modifiedUser = underTest.getUserEntry("u");
        assertTrue(modifiedUser.validateCredentials("u", "p"));
        assertEquals(user, modifiedUser);

        assertEquals(null,  underTest.getUserEntry(pair.username));

    }

    @Test
    public void shouldBeAbleToDeleteClientFromDatabase() throws IOException {
        underTest.createNewClient("c");
        String outputFile = "src/test/java/Server/AccountManagement/clientNewUserTest2.txt";
        underTest.generateNewUsersForClient("c", 1, outputFile);
        UsernamePasswordPair user = getCredentialsFromFile(outputFile);
        underTest.addSiteForClient("c", "s");

        underTest.deleteClient("c");

        assertEquals(null, underTest.getUserEntry(user.username));
        assertEquals(null, underTest.getSiteEntryForClient("c", "s"));
        assertFalse(underTest.getClientNames().contains("c"));
    }

}