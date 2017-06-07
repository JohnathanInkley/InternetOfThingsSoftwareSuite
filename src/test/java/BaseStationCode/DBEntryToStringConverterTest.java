package BaseStationCode;

import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DBEntryToStringConverterTest {

    @Test
    public void canConvertEntryToStringAndBack() throws IOException {
        DBEntryToStringConverter converter = new DBEntryToStringConverter();

        DatabaseEntry entry = new DatabaseEntry();
        entry.add("temperature", 30);
        entry.add("pet", "dog");

        String entryAsString = converter.convertToString(entry);

        assertEquals(entry, converter.convertToEntry(entryAsString));
    }

    @Test
    public void throwsIOExceptionWhenBadStringIsParsed() {
        try {
            DBEntryToStringConverter converter = new DBEntryToStringConverter();
            DatabaseEntry entry = converter.convertToEntry("badStringThisShouldNotWork");
            fail("This is in no way a java object as a string");
        } catch (IOException e) {
            // Successfully caught exception
        }

    }

}