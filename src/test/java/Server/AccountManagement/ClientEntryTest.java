package Server.AccountManagement;

import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import static Server.DatabaseStuff.ClientDatabaseEditor.*;
import static org.junit.Assert.assertEquals;


public class ClientEntryTest {

    @Test
    public void shouldBeAbleToGetAClientWithSitesFromDbEntry() {
        DatabaseEntry entry = new DatabaseEntry();
        entry.setTimestamp("1970-01-01 01:00:01.000");
        entry.add(CLIENT_FIELD_LABEL, "a");
        entry.add("site1", "factory");
        entry.add(TABLE_LABEL, CLIENT_SITE_TABLE_NAME);

        ClientEntry underTest = ClientEntry.getClientEntryFromSiteDbEntry(entry);
        assertEquals(1, underTest.getId());
        assertEquals(1, underTest.getSites().size());
        assertEquals("factory", underTest.getSites().get(0));
        assertEquals("a", underTest.getName());
    }

    @Test
    public void shouldBeAbleToGetSiteDbEntryFromClient() {
        ClientEntry underTest = new ClientEntry();
        underTest.setName("a");
        underTest.setId(1);
        underTest.addSite("factory");

        DatabaseEntry entry = new DatabaseEntry();
        entry.setTimestamp("1970-01-01 01:00:01.000");
        entry.add(CLIENT_FIELD_LABEL, "a");
        entry.add("site1", "factory");
        entry.add(TABLE_LABEL, CLIENT_SITE_TABLE_NAME);

        assertEquals(entry, underTest.getSiteDbEntry());
    }

    @Test
    public void shouldBeAbleToGetUserDbEntryFromClient() {
        ClientEntry underTest = new ClientEntry();
        underTest.setName("a");
        underTest.setId(1);
        underTest.addUser("u");

        DatabaseEntry entry = new DatabaseEntry();
        entry.setTimestamp("1970-01-01 01:00:01.000");
        entry.add(CLIENT_FIELD_LABEL, "a");
        entry.add("user1", "u");
        entry.add(TABLE_LABEL, CLIENT_USER_TABLE_NAME);

        assertEquals(entry, underTest.getUserDbEntry());
    }

    @Test
    public void shouldBeAbleToGetClientWithUsersFromDbEntry() {
        DatabaseEntry entry = new DatabaseEntry();
        entry.setTimestamp("1970-01-01 01:00:01.000");
        entry.add(CLIENT_FIELD_LABEL, "a");
        entry.add("user1", "u");
        entry.add(TABLE_LABEL, CLIENT_USER_TABLE_NAME);

        ClientEntry underTest = ClientEntry.getClientEntryFromUserDbEntry(entry);
        assertEquals(1, underTest.getId());
        assertEquals(1, underTest.getUsers().size());
        assertEquals("u", underTest.getUsers().get(0));
        assertEquals("a", underTest.getName());
    }

}