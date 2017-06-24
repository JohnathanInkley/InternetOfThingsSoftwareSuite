package Server.DatabaseStuff;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ClientDatabaseEditorTest {

    @Test
    public void shouldBeAbleToAddSitesForClientAndGetThemOut() {
        Database database = new Database("ClientManagementDatabaseTest", "http://localhost:8086/");

        ClientDatabaseEditor underTest = new ClientDatabaseEditor(database);
        underTest.createNewClient("Widgets Limited");
        underTest.addSiteForClient("Widgets Limited", "Widget Factory");
        underTest.addSiteForClient("Widgets Limited", "Wodget Factory");
        List<String> clientSites = underTest.getSitesForClient("Widgets Limited");
        assertTrue(clientSites.contains("Widget Factory"));
        assertTrue(clientSites.contains("Wodget Factory"));

//        database.deleteDatabase("ClientManagementDatabaseTest");
    }

    @Test
    public void shouldBeAbleToGetListOfClients() {
        Database database = new Database("ClientManagementDatabaseTest1", "http://localhost:8086/");

        ClientDatabaseEditor underTest = new ClientDatabaseEditor(database);
        underTest.createNewClient("client1");
        underTest.createNewClient("client2");
        List<String> clientList = underTest.getClientNames();
        assertTrue(clientList.contains("client1"));
        assertTrue(clientList.contains("client2"));
        database.deleteDatabase("ClientManagementDatabaseTest1");
    }
}