package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        return (userName == this.userName && BCrypt.checkpw(password, hashedPassword));
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
}
