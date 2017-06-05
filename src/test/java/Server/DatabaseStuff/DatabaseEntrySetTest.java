package Server.DatabaseStuff;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class DatabaseEntrySetTest {

    @Test
    public void shouldBeAbleToAddEntriesAndGetThemOut() {
        DatabaseEntrySet underTest = new DatabaseEntrySet();
        DatabaseEntry entry = new DatabaseEntry();
        underTest.add(entry);

        assertEquals(underTest.get(0), entry);
    }

    @Test
    public void shouldReturnCorrectSize() {
        DatabaseEntrySet underTest = new DatabaseEntrySet();
        underTest.add(new DatabaseEntry());
        underTest.add(new DatabaseEntry());

        assertEquals(2, underTest.size());
    }

    @Test
    public void tryingToAccessNonExistentElementShouldThrowException() {
        DatabaseEntrySet underTest = new DatabaseEntrySet();

        try {
            underTest.get(1);
            fail("Exception should be thrown due to non existent element");
        } catch (IndexOutOfBoundsException e) {}
    }

    @Test
    public void setsWithSameEntriesShouldBeEqual() {
        DatabaseEntrySet underTest = new DatabaseEntrySet();
        DatabaseEntry sampleEntry = new DatabaseEntry();
        underTest.add(sampleEntry);

        DatabaseEntrySet otherSet = new DatabaseEntrySet();
        otherSet.add(sampleEntry);

        assertEquals(otherSet, underTest);
    }

    @Test
    public void shouldBeAbleToJoinTwoEntrySets() {
        DatabaseEntrySet set1 = new DatabaseEntrySet();
        DatabaseEntrySet set2 = new DatabaseEntrySet();

        DatabaseEntry entry1 = new DatabaseEntry();
        entry1.add("pet", "cat");
        DatabaseEntry entry2 = new DatabaseEntry();
        entry2.add("pet", "dog");

        set1.add(entry1);
        set2.add(entry2);

        DatabaseEntrySet underTest = set1.join(set2);

        assertEquals(underTest.get(0), entry1);
        assertEquals(underTest.get(1), entry2);
    }
}