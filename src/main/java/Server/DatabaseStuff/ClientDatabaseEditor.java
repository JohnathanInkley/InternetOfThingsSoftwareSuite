package Server.DatabaseStuff;

import java.util.ArrayList;
import java.util.List;

public class ClientDatabaseEditor {
    private static final String CLIENT_SITE_TABLE_NAME = "clientToSiteTable";
    private static final String CLIENT_USER_TABLE_NAME = "clientToUsersTable";
    private static final String CLIENT_FIELD_LABEL = "client";
    private static final String TABLE_LABEL = "DeviceCollection";

    private Database database;
    private DatabaseEntry clientEntryForSites;
    private DatabaseEntry clientEntryForUsers;
    private String numberClientsSoFarAsString;

    public ClientDatabaseEditor(Database database) {
        this.database = database;
        clientEntryForSites = new DatabaseEntry();
        clientEntryForSites.add(TABLE_LABEL, CLIENT_SITE_TABLE_NAME);
        clientEntryForSites.useTimestampAsId();
        clientEntryForUsers = new DatabaseEntry();
        clientEntryForUsers.add(TABLE_LABEL, CLIENT_USER_TABLE_NAME);
        clientEntryForUsers.useTimestampAsId();
    }

    public void createNewClient(String clientName) {
        numberClientsSoFarAsString = String.valueOf(getClientNames().size());
        populateDatabaseEntryForClient(clientName, clientEntryForSites);
        populateDatabaseEntryForClient(clientName, clientEntryForUsers);
    }

    private void populateDatabaseEntryForClient(String clientName, DatabaseEntry clientEntry) {
        clientEntry.setTimestamp(numberClientsSoFarAsString);
        clientEntry.add(CLIENT_FIELD_LABEL, clientName);
        database.addEntry(clientEntry);
    }

    public void addSiteForClient(String clientName, String siteName) {
        DatabaseEntry entry = database.getEntries(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName).get(0);
        int numberOfSites = entry.getNumberOfFields() - 2;
        entry.add("site" + numberOfSites, siteName);
        database.removeAllWithCertainValue(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName);
        database.addEntry(entry);
    }

    public List<String> getSitesForClient(String clientName) {
        DatabaseEntry entry = database.getEntries(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName).get(0);
        List<String> siteList = new ArrayList<>();
        for (int i = 0; i < entry.getNumberOfFields() - 2; i++) {
            siteList.add((String) entry.get("site" + (i + 1)));
        }
        return siteList;
    }

    public List<String> getClientNames() {
        DatabaseEntrySet clientEntries = database.getEntries(CLIENT_SITE_TABLE_NAME,
                                                            TABLE_LABEL,
                                                            CLIENT_SITE_TABLE_NAME);
        List<String> clientList = new ArrayList<>();
        for (int i = 0; i < clientEntries.size(); i++) {
            clientList.add((String) clientEntries.get(i).get(CLIENT_FIELD_LABEL));
        }
        return clientList;
    }

}
