package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static Server.DatabaseStuff.ClientDatabaseEditor.CLIENT_FIELD_LABEL;
import static Server.DatabaseStuff.ClientDatabaseEditor.SUFFIX_FOR_CLIENTS_USER_TABLE;
import static Server.DatabaseStuff.ClientDatabaseEditor.TABLE_LABEL;
import static Server.DatabaseStuff.DatabaseEntry.timestampFormat;

public class UserEntry {
    public static final int DEFAULT_USERNAME_LENGTH = 20;
    public static final int DEFAULT_PASSWORD_LENGTH = 30;
    public static final String HASHED_PASSWORD_LABEL = "hashedPassword";
    public static final String USERNAME_LABEL = "userName";
    public static final String FIRST_NAME_LABEL = "firstName";
    public static final String LAST_NAME_LABEL = "lastName";
    public static final String EMAIL_LABEL = "email";

    private String hashedPassword;
    private String clientName;
    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> sitesHavePermissionFor;

    private boolean built;

    public static UserEntry generateUnbuiltUser(String clientName) {
        return new UserEntry(clientName);
    }

    private UserEntry(String clientName) {
        this.clientName = clientName;
        sitesHavePermissionFor = new ArrayList<>();
        built = false;
    }

    private UserEntry() {
        sitesHavePermissionFor = new ArrayList<>();
    }

    public UserNamePasswordPair generateDefaultPasswordAndBuild() {
        if (id == null) {
            throw new RuntimeException("ID must be set before default username and password can be generated");
        }
        userName = SecureRandomStringGenerator.generateSecureRandomString(DEFAULT_USERNAME_LENGTH);
        String defaultPassword = SecureRandomStringGenerator.generateSecureRandomString(DEFAULT_PASSWORD_LENGTH);
        UserNamePasswordPair defaultAccountDetails = new UserNamePasswordPair(userName, defaultPassword);
        setPasswordAndHash(defaultPassword);
        built = true;
        return defaultAccountDetails;
    }

    public void setPasswordAndHash(String rawPassword) {
        String salt = BCrypt.gensalt(12);
        hashedPassword = BCrypt.hashpw(rawPassword, salt);
    }

    String getHashedPassword() {
        return hashedPassword;
    }

    public String getClientName() {
        return clientName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public DatabaseEntry getDbEntry() {
        if (!built) {
            throw new RuntimeException("Cannot get entry for unbuilt user");
        }
        DatabaseEntry result = new DatabaseEntry();
        addFieldIfNotNull(result, TABLE_LABEL, clientName + "." + SUFFIX_FOR_CLIENTS_USER_TABLE);
        addFieldIfNotNull(result, HASHED_PASSWORD_LABEL, hashedPassword);
        addFieldIfNotNull(result, USERNAME_LABEL, userName);
        addFieldIfNotNull(result, FIRST_NAME_LABEL, firstName);
        addFieldIfNotNull(result, LAST_NAME_LABEL, lastName);
        addFieldIfNotNull(result, EMAIL_LABEL, email);
        addFieldIfNotNull(result, CLIENT_FIELD_LABEL, clientName);
        addSitesToEntry(result);
        result.setTimestamp(timestampFormat.format(new Date(id*1000)));
        return result;
    }

    private void addFieldIfNotNull(DatabaseEntry entry, String field, Object value) {
        if (value != null) {
            entry.add(field, value);
        }
    }

    private void addSitesToEntry(DatabaseEntry result) {
        int siteCount = 1;
        for (String site : sitesHavePermissionFor) {
            result.add("site" + siteCount++, site);
        }
    }

    public boolean validateCredentials(String userName, String password) {
        return (userName.equals(this.userName) && BCrypt.checkpw(password, hashedPassword));
    }


    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void giveSitePermission(String siteName) {
        sitesHavePermissionFor.add(siteName);
    }

    public boolean hasPermissionForSite(String siteName) {
        return sitesHavePermissionFor.contains(siteName);
    }

    public static UserEntry getUserFromDbEntry(DatabaseEntry entry) {
        UserEntry user = new UserEntry();
        user.built = true;
        user.firstName = (String) entry.get(FIRST_NAME_LABEL);
        user.lastName = (String) entry.get(LAST_NAME_LABEL);
        user.email = (String) entry.get(EMAIL_LABEL);
        user.hashedPassword = (String) entry.get(HASHED_PASSWORD_LABEL);
        user.userName = (String) entry.get(USERNAME_LABEL);
        getSitesFromDbEntry(user, entry);
        user.clientName = getClientNameFromDbEntry(entry);
        user.id = getIdFromDbEntry(entry);
        return user;
    }


    private static void getSitesFromDbEntry(UserEntry user, DatabaseEntry entry) {
        int siteCount = 1;
        String site = (String) entry.get("site" + siteCount++);
        while (site != null) {
            user.sitesHavePermissionFor.add(site);
            site = (String) entry.get("site" + siteCount++);
        }
    }

    private static String getClientNameFromDbEntry(DatabaseEntry entry) {
        String rawTableName = (String) entry.get(TABLE_LABEL);
        return rawTableName.split("\\.")[0];
    }

    private static Long getIdFromDbEntry(DatabaseEntry entry) {
        try {
            return (timestampFormat.parse(entry.getTimestamp()).getTime()
                    - timestampFormat.parse("1970-01-01 01:00:00.000").getTime())/1000;
        } catch (ParseException e) {
            throw new RuntimeException("Database entry has incorrect timestamp format");
        }
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEntry userEntry = (UserEntry) o;

        if (built != userEntry.built) return false;
        if (!hashedPassword.equals(userEntry.hashedPassword)) return false;
        if (!clientName.equals(userEntry.clientName)) return false;
        if (!id.equals(userEntry.id)) return false;
        if (!userName.equals(userEntry.userName)) return false;
        if (firstName != null ? !firstName.equals(userEntry.firstName) : userEntry.firstName != null) return false;
        if (lastName != null ? !lastName.equals(userEntry.lastName) : userEntry.lastName != null) return false;
        if (email != null ? !email.equals(userEntry.email) : userEntry.email != null) return false;
        return sitesHavePermissionFor.equals(userEntry.sitesHavePermissionFor);
    }

    @Override
    public int hashCode() {
        int result = clientName.hashCode();
        result = 31 * result + userName.hashCode();
        return result;
    }

}
