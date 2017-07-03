package Server.DatabaseStuff;

import Server.AccountManagement.*;
import Server.PhysicalLocationStuff.SensorLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static Server.AccountManagement.UserEntry.USERNAME_LABEL;
import static Server.AccountManagement.UserEntry.getUserFromDbEntry;

public class ClientDatabaseEditor {
    public static final String CLIENT_SITE_TABLE_NAME = "clientToSiteTable";
    public static final String CLIENT_USER_TABLE_NAME = "clientToUsersTable";
    public static final String CLIENT_FIELD_LABEL = "client";
    public static final String TABLE_LABEL = "DeviceCollection";
    public static final String SUFFIX_FOR_CLIENTS_USER_TABLE = "userList";
    private static final String TEMPLATE_CONFIG_FILE = "src/main/java/Server/AccountManagement/template.config";
    private static final String BASE_STATION_SERVER_URL = "http://localhost:8080/SensorServer/server";

    private Database database;
    private SiteConfigFileGenerator siteConfigGenerator;
    private UserConfigFileGenerator userConfigGenerator;

    public ClientDatabaseEditor(Database database) {
        this.database = database;
        siteConfigGenerator = new SiteConfigFileGenerator(TEMPLATE_CONFIG_FILE);
        userConfigGenerator = new UserConfigFileGenerator();
    }

    public void createNewClient(String clientName) {
        ClientEntry client = new ClientEntry();
        client.setName(clientName);
        client.setId(getNumberOfClients()+1);
        database.addEntry(client.getSiteDbEntry());
        database.addEntry(client.getUserDbEntry());
    }

    public void addSiteForClient(String clientName, String siteName) {
        ClientEntry client = getClientSitesEntry(clientName);
        client.addSite(siteName);
        database.addEntry(client.getSiteDbEntry());
    }

    public void generateConfigFileForSite(String clientName, String siteName, String configFilePath) {
        siteConfigGenerator.initialiseNewConfig();
        siteConfigGenerator.setClientAndSiteName(clientName, siteName);
        siteConfigGenerator.setServerAddress(BASE_STATION_SERVER_URL);
        siteConfigGenerator.writeFile(configFilePath);
        createTableForSiteInDatabase(clientName, siteName);
    }

    private void createTableForSiteInDatabase(String clientName, String siteName) {
        String aesString = siteConfigGenerator.getAesString();
        SiteEntry newSite = new SiteEntry(clientName, siteName);
        newSite.addAesString(aesString);
        database.addEntry(newSite.getDbEntries().get(0));
    }

    private ClientEntry getClientSitesEntry(String clientName) {
        DatabaseEntry entry = database.getEntriesWithCertainValueFromTable(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName).get(0);
        return ClientEntry.getClientEntryFromSiteDbEntry(entry);
    }


    public HashMap<String,String> getAesKeysForSites() {
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
        ClientEntry client = getClientSitesEntry(clientName);
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

    public SiteEntry getSiteEntryForClient(String clientName, String siteName) {
        String clientSiteIdentifier = clientName + "." + siteName;
        DatabaseEntrySet siteEntries = database.getEntriesWithCertainValueFromTable(clientSiteIdentifier, TABLE_LABEL, clientSiteIdentifier);
        SiteEntry site;
        if (siteEntries.isEmpty()) {
            site = null;
        } else {
            site = SiteEntry.getSiteFromDbEntrySet(siteEntries);
        }
        return site;
    }

    public void generateNewUsersForClient(String clientName, int numberOfNewUsers, String outputFile) {
        generateNewAccountsForClient(clientName, numberOfNewUsers, outputFile, false);
    }


    public void generateNewAdminsForClient(String clientName, int numberOfNewAdmins, String outputFile) {
       generateNewAccountsForClient(clientName, numberOfNewAdmins, outputFile, true);
    }

    private void generateNewAccountsForClient(String clientName, int numberOfNewAccounts, String outputFile, boolean isAdmin) {
        userConfigGenerator.initialiseNewConfigFile();

        long maxIdOfCurrentUsers = getMaxIdOfCurrentUsers(clientName);

        for (int i = 1; i <= numberOfNewAccounts; i++) {
            UserEntry newUser = createNewUserEntry(clientName, maxIdOfCurrentUsers, i, isAdmin);
            addUserToClientUserTable(newUser);
            database.addEntry(newUser.getDbEntry());
        }
        userConfigGenerator.generateOutputFile(outputFile);
    }

    private long getMaxIdOfCurrentUsers(String clientName) {
        String userTableName = clientName + "." + SUFFIX_FOR_CLIENTS_USER_TABLE;
        DatabaseEntrySet userEntries = database.getEntriesWithCertainValueFromTable(userTableName, TABLE_LABEL, userTableName);
        return (long) userEntries.size();
    }

    private UserEntry createNewUserEntry(String clientName, long maxIdOfCurrentUsers, int i, boolean createAsAdmin) {
        UserEntry newUser = UserEntry.generateUnbuiltUser(clientName);
        newUser.setId(maxIdOfCurrentUsers + i);
        if (createAsAdmin) {
            newUser.setAdminFlag();
        }
        UsernamePasswordPair credentials = newUser.generateDefaultPasswordAndBuild();
        userConfigGenerator.addUser(credentials);
        return newUser;
    }

    public UserEntry getUserEntry(String username) {
        DatabaseEntrySet matchingUsers = database.getAllEntriesWithCertainValue(USERNAME_LABEL, username);
        if (matchingUsers.size() == 0) {
          return null;
        } else if (matchingUsers.size() > 1) {
            throw new RuntimeException("More than one user with same username, please check situation");
        }
        UserEntry user = getUserFromDbEntry(matchingUsers.get(0));
        if (user.isAdmin()) {
            List<String> clientSites = getSiteNamesForClient(user.getClientName());
            for (String site : clientSites) {
                user.giveSitePermission(site);
            }
        }
        return user;
    }

    private void addUserToClientUserTable(UserEntry newUser) {
        ClientEntry client = getClientUsersEntry(newUser.getClientName());
        client.addUser(newUser.getUsername());
        database.addEntry(client.getUserDbEntry());
    }

    private ClientEntry getClientUsersEntry(String clientName) {
        DatabaseEntry entry = database.getEntriesWithCertainValueFromTable(CLIENT_USER_TABLE_NAME, CLIENT_FIELD_LABEL, clientName).get(0);
        return ClientEntry.getClientEntryFromUserDbEntry(entry);
    }

    public List<String> getUserNamesForClient(String clientName) {
        ClientEntry client = getClientUsersEntry(clientName);
        return client.getUsers();
    }

    public void addUserEntry(UserEntry user) {
        DatabaseEntry modifiedUserEntry = user.getDbEntry();
        DatabaseEntry existingUserEntry = database.getSiteEntriesBetween(
                modifiedUserEntry.getDeviceCollectionIdentifier(),
                modifiedUserEntry.getLongTimeInMilliseconds(),
                modifiedUserEntry.getLongTimeInMilliseconds())
                .get(0);
        ClientEntry clientOwningUser = getClientUsersEntry(user.getClientName());
        clientOwningUser.removeUser(UserEntry.getUserFromDbEntry(existingUserEntry).getUsername());
        clientOwningUser.addUser(user.getUsername());
        database.addEntry(clientOwningUser.getUserDbEntry());
        database.addEntry(modifiedUserEntry);
    }

    public void deleteClient(String clientName) {
        deleteClientSiteTable(clientName);
        deleteClientUserTable(clientName);
        deleteClientFromClientTable(clientName);
    }

    private void deleteClientSiteTable(String clientName) {
        List<String> siteNamesForClient = getSiteNamesForClient(clientName);
        for (String siteName : siteNamesForClient) {
            String tableName = clientName + "." + siteName;
            database.removeAllFromGivenTable(tableName);
        }
    }

    private void deleteClientUserTable(String clientName) {
        database.removeAllFromGivenTable(clientName +  "." + SUFFIX_FOR_CLIENTS_USER_TABLE);
    }

    private void deleteClientFromClientTable(String clientName) {
        DatabaseEntrySet allEntries = database.getAllEntriesWithCertainValue(CLIENT_FIELD_LABEL, clientName);
        for (int i = 0; i < allEntries.size(); i++) {
            DatabaseEntry entry = allEntries.get(i);
            DatabaseEntry replacementEntry = new DatabaseEntry();
            replacementEntry.setTimestamp(entry.getTimestamp());
            for (DatabaseEntryField field : entry) {
                replacementEntry.add(field.getFieldName(), "");
            }
            replacementEntry.add(TABLE_LABEL, entry.get(TABLE_LABEL));
            database.addEntry(replacementEntry);
        }

        //database.removeAllWithCertainValue(CLIENT_SITE_TABLE_NAME, CLIENT_FIELD_LABEL, clientName);
        //database.removeAllWithCertainValue(CLIENT_USER_TABLE_NAME, CLIENT_FIELD_LABEL, clientName);
    }

}
