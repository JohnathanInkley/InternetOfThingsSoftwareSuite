package Server.DatabaseStuff;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        List<String> clientSites = underTest.getSitesForClient("c");
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
        // Will come back once I've designed a site object
    }

    @Test
    public void shouldBeAbleToAddUsersForClient() {

    }

}