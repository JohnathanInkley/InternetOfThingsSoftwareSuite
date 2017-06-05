package Server.DatabaseStuff;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class DatabaseEntryTest {

    @Test
    public void fieldsCanBeIteratedThrough() {
        DatabaseEntry underTest = new DatabaseEntry();
        DatabaseEntryField field1 = new DatabaseEntryField("field1", new Double(0.5));
        DatabaseEntryField field2 = new DatabaseEntryField("field2", "fieldValue");
        underTest.add(field1.getFieldName(), field1.getFieldValue());
        underTest.add(field2.getFieldName(), field2.getFieldValue());

        Iterator<DatabaseEntryField> it = underTest.iterator();
        assertEquals(field1, it.next());
        assertEquals(field2, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void fieldValuesCanBeGotByName() {
        DatabaseEntry underTest = new DatabaseEntry();
        underTest.add("field", new Double(0.5));
        assertEquals(new Double(0.5), underTest.get("field"));
    }

    @Test
    public void correctNumberOfEntriesCanBeCalculated() {
        DatabaseEntry underTest = new DatabaseEntry();
        DatabaseEntryField field1 = new DatabaseEntryField("field1", new Double(0.5));
        DatabaseEntryField field2 = new DatabaseEntryField("field2", "fieldValue");
        underTest.add(field1.getFieldName(), field1.getFieldValue());
        underTest.add(field2.getFieldName(), field2.getFieldValue());

        assertEquals(2, underTest.numberOfFields());
    }

    @Test
    public void entriesCanBeComparedForEquality() {
        DatabaseEntry underTest = new DatabaseEntry();
        underTest.add("field", "dog");

        DatabaseEntry equalEntry = new DatabaseEntry();
        equalEntry.add("field", "dog");
        assertEquals(equalEntry, underTest);

        DatabaseEntry tooManyFields = new DatabaseEntry();
        tooManyFields.add("field", "dog");
        tooManyFields.add("field1", new Double(0.5));
        assertFalse(underTest.equals(tooManyFields));

        DatabaseEntry rightFieldNameAndRightValueClassButWrongValue = new DatabaseEntry();
        rightFieldNameAndRightValueClassButWrongValue.add("field", "cat");
        assertFalse(underTest.equals(rightFieldNameAndRightValueClassButWrongValue));

        DatabaseEntry rightFieldNameButWrongFieldClass = new DatabaseEntry();
        rightFieldNameButWrongFieldClass.add("field", new Double(0.5));
        assertFalse(underTest.equals(rightFieldNameButWrongFieldClass));

        DatabaseEntry wrongFieldName = new DatabaseEntry();
        wrongFieldName.add("notSameField", "dog");
        assertFalse(underTest.equals(wrongFieldName));
    }

    @Test
    public void entrySitesCanBeSet() {
        DatabaseEntry underTest = new DatabaseEntry();
        DeviceCollection site = new DeviceCollection("owner", "site");
        underTest.setDeviceCollectionIdentifier(site);

        assertEquals(site.identifier(), underTest.getDeviceCollectionIdentifier());

    }

    @Test
    public void timestampsCanBeSet() {
        DatabaseEntry underTest = new DatabaseEntry();
        String timestamp = ("2000-01-23 12:34:56.789");
        underTest.setTimestamp(timestamp);

        assertEquals(timestamp, underTest.getTimestamp());
    }

    @Test
    public void timestampFormatsCanBeSetToGetTimeOutCorrectly() {
        DatabaseEntry underTest = new DatabaseEntry();
        String timestamp = ("1970-01-01 01:00:05.000");
        underTest.setTimestamp(timestamp);
        underTest.setTimestampFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

        assertEquals(5000, underTest.getLongTimeInMilliseconds());
    }
}