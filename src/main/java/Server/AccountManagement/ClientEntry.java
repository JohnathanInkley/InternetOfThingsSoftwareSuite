package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;

import java.util.ArrayList;
import java.util.Date;

import static Server.DatabaseStuff.ClientDatabaseEditor.CLIENT_FIELD_LABEL;
import static Server.DatabaseStuff.ClientDatabaseEditor.CLIENT_SITE_TABLE_NAME;
import static Server.DatabaseStuff.ClientDatabaseEditor.TABLE_LABEL;
import static Server.DatabaseStuff.DatabaseEntry.timestampFormat;

public class ClientEntry {

    private String name;
    private long clientId;
    ArrayList<String> sites;
    ArrayList<String> users;

    public ClientEntry() {
        sites = new ArrayList<>();
        users = new ArrayList<>();
    }

    public static ClientEntry getClientEntryFromSiteDbEntry(DatabaseEntry entry) {
        try {
            ClientEntry result = new ClientEntry();
            result.name = (String) entry.get("client");
            result.populateSitesFromDbEntry(entry);
            result.clientId = (timestampFormat.parse(entry.getTimestamp() + ".000").getTime()
                    - timestampFormat.parse("1970-01-01 00:00:00.000").getTime())/1000;
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Client entry could not be created due to timestamp issues in database");
        }
    }

    public void populateSitesFromDbEntry(DatabaseEntry entry) {
        int numberFieldsInEntry = entry.getNumberOfFields();
        int numberOfSitesInEntry = numberFieldsInEntry - 2;
        for (int i = 1; i <= numberOfSitesInEntry; i++) {
            sites.add((String) entry.get("site" + i));
        }
    }

    public void populateClientsFromDbEntry(DatabaseEntry entry) {
        int numberOfClients;
    }

    public long getId() {
        return clientId;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getSites() {
        return sites;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.clientId = id;
    }

    public void addSite(String siteName) {
        sites.add(siteName);
    }

    public DatabaseEntry getSiteDbEntry() {
        DatabaseEntry result = new DatabaseEntry();
        result.setTimestamp(timestampFormat.format(new Date(clientId*1000)));
        result.add(CLIENT_FIELD_LABEL, name);
        result.add(TABLE_LABEL, CLIENT_SITE_TABLE_NAME);
        for (int i = 1; i <= sites.size(); i++) {
            result.add("site" + i, sites.get(i - 1));
        }
        return result;
    }
}
