package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import static Server.AccountManagement.UserEntry.*;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserEntryTest {

    @Test
    public void shouldBeAbleToCreateUserForClientAndGetDefaultAccountCredentials() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(2);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        assertEquals(30, defaultAccountDetails.password.length());
        assertEquals(20, defaultAccountDetails.username.length());
        assertEquals("client", underTest.getClientName());
        assertEquals(2, underTest.getId());
    }

    @Test
    public void buildingUserWithNoIdShouldThrowException() {
        try {
            UserEntry underTest = UserEntry.generateUnbuiltUser("client");
            underTest.generateDefaultPasswordAndBuild();
            fail("no id has been set so should fail");
        } catch (Exception e) {
            assertEquals("ID must be set before default username and password can be generated", e.getMessage());
        }
    }

    @Test
    public void shouldBeAbleToGetDatabaseEntryForInititalisedUserEntry() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        underTest.setAdminFlag();
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        DatabaseEntry expected = new DatabaseEntry();
        expected.add("DeviceCollection", "client.userList");
        expected.add(HASHED_PASSWORD_LABEL, underTest.getHashedPassword());
        expected.add(USERNAME_LABEL, defaultAccountDetails.username);
        expected.add("client", "client");
        expected.add(ADMIN_FLAG, "true");
        expected.setTimestamp("1970-01-01 01:00:03.000");

        assertEquals(expected, underTest.getDbEntry());
    }

    @Test
    public void gettingDbEntryForUnbuiltUserShouldThrowException() {
        try {
            UserEntry underTest = UserEntry.generateUnbuiltUser("client");
            underTest.getDbEntry();
            fail("Unbuilt user cannot produce db entry");
        } catch (Exception e) {
            assertEquals("Cannot get entry for unbuilt user", e.getMessage());
        }
    }

    @Test
    public void defaultUserNameAndPasswordShouldValidateCorrectly() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        assertTrue(underTest.validateCredentials(defaultAccountDetails.username, defaultAccountDetails.password));
    }

    @Test
    public void shouldBeAbleToChangeUsernameAfterCreation() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        assertEquals(defaultAccountDetails.username, underTest.getUsername());
        underTest.setUsername("user");
        assertEquals("user", underTest.getUsername());
    }

    @Test
    public void shouldBeAbleToChangePasswordAfterCreation() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        underTest.setPasswordAndHash("newPassword");
        underTest.setUsername("user");
        assertTrue(underTest.validateCredentials("user", "newPassword"));
    }

    @Test
    public void shouldBeAbleToSetNamesAndEmailAddress() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        underTest.setFirstName("John");
        underTest.setLastName("Smith");
        underTest.setEmail("hi@gmail.com");

        DatabaseEntry entry = underTest.getDbEntry();

        assertEquals("John", entry.get(FIRST_NAME_LABEL));
        assertEquals("Smith", entry.get(LAST_NAME_LABEL));
        assertEquals("hi@gmail.com", entry.get(EMAIL_LABEL));
    }

    @Test
    public void shouldBeAbleToGiveSitePermissionsAndCheckIfSiteIsOnList() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        underTest.giveSitePermission("siteName");
        assertTrue(underTest.hasPermissionForSite("siteName"));
        assertFalse(underTest.hasPermissionForSite("badSite"));
    }

    @Test
    public void shouldHaveSitesPermissionsInDbEntry() {
        UserEntry underTest = UserEntry.generateUnbuiltUser("client");
        underTest.setId(3);
        UsernamePasswordPair defaultAccountDetails = underTest.generateDefaultPasswordAndBuild();

        underTest.giveSitePermission("siteName1");
        underTest.giveSitePermission("siteName2");

        DatabaseEntry entry = underTest.getDbEntry();

        assertEquals("siteName1", entry.get("site1"));
        assertEquals("siteName2", entry.get("site2"));
    }

    @Test
    public void shouldBeAbleToGenerateUserFromDbEntry() {
        DatabaseEntry entry = new DatabaseEntry();
        entry.add("DeviceCollection", "client.userList");
        entry.add(HASHED_PASSWORD_LABEL, "password");
        entry.add(USERNAME_LABEL, "username");
        entry.add("site1", "s1");
        entry.add("site2", "s2");
        entry.add("email", "hi@gmail.com");
        entry.add(FIRST_NAME_LABEL, "first");
        entry.add(LAST_NAME_LABEL, "last");
        entry.add("client", "client");
        entry.add(ADMIN_FLAG, "true");
        entry.setTimestamp("1970-01-01 01:00:03.000");

        UserEntry user = UserEntry.getUserFromDbEntry(entry);

        assertEquals(entry, user.getDbEntry());
    }

}