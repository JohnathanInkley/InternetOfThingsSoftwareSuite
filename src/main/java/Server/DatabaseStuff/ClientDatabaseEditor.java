package Server.DatabaseStuff;

import java.util.ArrayList;
import java.util.List;

public class ClientDatabaseEditor {
    private Database database;
    private DatabaseEntry clientEntryForSites;
    private DatabaseEntry clientEntryForUsers;

    public ClientDatabaseEditor(Database database) {
        this.database = database;
        clientEntryForSites = new DatabaseEntry();
        clientEntryForSites.add("DeviceCollection", "clientToSiteTable");
        clientEntryForSites.useTimestampAsId();
        clientEntryForUsers = new DatabaseEntry();
        clientEntryForUsers.add("DeviceCollection", "clientToUsersTable");
        clientEntryForUsers.useTimestampAsId();
    }

    public void createNewClient(String clientName) {
        int numberClientsSoFar = getClientNames().size();
        clientEntryForSites.setTimestamp(String.valueOf(numberClientsSoFar));
        clientEntryForUsers.setTimestamp(String.valueOf(numberClientsSoFar));
        clientEntryForSites.add("client", clientName);
        clientEntryForUsers.add("client", clientName);
        database.addEntry(clientEntryForSites);
        database.addEntry(clientEntryForUsers);
    }

    public void addSiteForClient(String clientName, String siteName) {
        DatabaseEntry entry = database.getEntries("clientToSiteTable", "client", clientName).get(0);
        int numberOfSites = entry.getNumberOfFields() - 2;
        entry.add("site" + numberOfSites, siteName);
        database.removeAllWithCertainValue("clientToSiteTable", "client", clientName);
        database.addEntry(entry);
    }

    public List<String> getSitesForClient(String clientName) {
        DatabaseEntry entry = database.getEntries("clientToSiteTable", "client", clientName).get(0);
        List<String> siteList = new ArrayList<>();
        for (int i = 0; i < entry.getNumberOfFields() - 2; i++) {
            siteList.add((String) entry.get("site" + (i + 1)));
        }
        return siteList;
    }

    public List<String> getClientNames() {
        DatabaseEntrySet clientEntries = database.getEntries("clientToSiteTable",
                                                            "DeviceCollection",
                                                            "clientToSiteTable");
        List<String> clientList = new ArrayList<>();
        for (int i = 0; i < clientEntries.size(); i++) {
            clientList.add((String) clientEntries.get(i).get("client"));
        }
        return clientList;
    }

}
