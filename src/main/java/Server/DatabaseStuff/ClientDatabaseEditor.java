package Server.DatabaseStuff;

import Server.AccountManagement.ClientEntry;
import Server.AccountManagement.ConfigFileGenerator;
import Server.AccountManagement.SiteEntry;
import Server.PhysicalLocationStuff.SensorLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientDatabaseEditor {
    public static final String CLIENT_SITE_TABLE_NAME = "clientToSiteTable";
    public static final String CLIENT_USER_TABLE_NAME = "clientToUsersTable";
    public static final String CLIENT_FIELD_LABEL = "client";
    public static final String TABLE_LABEL = "DeviceCollection";
    public static final String SUFFIX_FOR_CLIENTS_USER_TABLE = "userList";
    private static final String TEMPLATE_CONFIG_FILE = "src/main/java/Server/AccountManagement/template.config";
    private static final String BASE_STATION_SERVER_URL = "http://localhost:8080/SensorServer/server";

    private Database database;
    private ConfigFileGenerator configGenerator;

    public ClientDatabaseEditor(Database database) {
        this.database = database;
        configGenerator = new ConfigFileGenerator(TEMPLATE_CONFIG_FILE);

    }

    public void createNewClient(String clientName) {
        ClientEntry client = new ClientEntry();
        client.setName(clientName);
        client.setId(getNumberOfClients()+1);
        database.addEntry(client.getSiteDbEntry());
    }

    public void addSiteForClient(String clientName, String siteName) {
        ClientEntry client = getClientEntry(clientName);
        client.addSite(siteName);
        database.addEntry(client.getSiteDbEntry());
    }

    public void generateConfigFileForSite(String clientName, String siteName, String configFilePath) {
        configGenerator.initialiseNewConfig();
        configGenerator.setClientAndSiteName(clientName, siteName);
        configGenerator.setServerAddress(BASE_STATION_SERVER_URL);
        configGenerator.writeFile(configFilePath);
        createTableForSiteInDatabase(clientName, siteName);
    }

    private void createTableForSiteInDatabase(String clientName, String siteName) {
        String aesString = configGenerator.getAesString();
        SiteEntry newSite = new SiteEntry(clientName, siteName);
        newSite.addAesString(aesString);
        database.addEntry(newSite.getDbEntries().get(0));
    }

    private ClientEntry getClientEntry(String clientName) {
        DatabaseEntry entry = database.getEntriesWithCertainValueFromTable(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName).get(0);
        return ClientEntry.getClientEntryFromSiteDbEntry(entry);
    }

    public HashMap<String,String> getAesKeys() {
        HashMap<String, String> results = new HashMap<>();
        DatabaseEntrySet aesStrings = database.getAllEntriesWithCertainValue("IP", SiteEntry.AES_STRING_ENTRY_LABEL);
        for (int i = 0; i < aesStrings.size(); i++) {
            DatabaseEntry entry = aesStrings.get(i);
            String aesString = (String) entry.get("aesString");
            String siteID = (String) entry.get(TABLE_LABEL);
            results.put(siteID, aesString);
        }
        return results;
    }

    public List<String> getSiteNamesForClient(String clientName) {
        ClientEntry client = getClientEntry(clientName);
        return client.getSites();
    }

    public List<String> getClientNames() {
        DatabaseEntrySet dbEntries = getAllClientEntries();
        List<String> clientList = new ArrayList<>();
        for (int i = 0; i < dbEntries.size(); i++) {
            ClientEntry client = ClientEntry.getClientEntryFromSiteDbEntry(dbEntries.get(i));
            clientList.add(client.getName());
        }
        return clientList;
    }

    private int getNumberOfClients() {
        DatabaseEntrySet clientEntries = getAllClientEntries();
        return clientEntries.size();
    }

    private DatabaseEntrySet getAllClientEntries() {
        return database.getEntriesWithCertainValueFromTable(CLIENT_SITE_TABLE_NAME,
                    TABLE_LABEL,
                    CLIENT_SITE_TABLE_NAME);
    }

    public void addSensorToClientSite(String clientName, String siteName, SensorLocation sensor) {
        SiteEntry site = getSiteEntryFromDatabaseForSite(clientName, siteName);
        DatabaseEntry entry = site.getDbEntryForSensor(sensor);
        database.addEntry(entry);
    }

    public List<SensorLocation> getSensorsForClientSite(String clientName, String siteName) {
        SiteEntry site = getSiteEntryFromDatabaseForSite(clientName, siteName);
        return site.getArrayOfSensors();
    }

    private SiteEntry getSiteEntryFromDatabaseForSite(String clientName, String siteName) {
        String clientSiteIdentifier = clientName + "." + siteName;
        DatabaseEntrySet siteEntries = database.getEntriesWithCertainValueFromTable(clientSiteIdentifier, TABLE_LABEL, clientSiteIdentifier);
        SiteEntry site;
        if (siteEntries.isEmpty()) {
            site = new SiteEntry(clientName, siteName);
        } else {
            site = SiteEntry.getSiteFromDbEntrySet(siteEntries);
        }
        return site;
    }
}
