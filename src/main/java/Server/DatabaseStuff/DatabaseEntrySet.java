package Server.DatabaseStuff;

import java.util.ArrayList;

public class DatabaseEntrySet {
    private ArrayList<DatabaseEntry> entries;

    public DatabaseEntrySet() {
        entries = new ArrayList<>();
    }


    public void add(DatabaseEntry entry) {
        entries.add(entry);
    }

    public DatabaseEntry get(int i) throws IndexOutOfBoundsException {
        if (i < size()) {
            return entries.get(i);
        }
        throw new IndexOutOfBoundsException("Set size is " + size() + " and index was " + i);
    }

    public int size() {
        return entries.size();
    }

    @Override
    public boolean equals(Object other) {
        DatabaseEntrySet otherSet = (DatabaseEntrySet) other;
        return entries.equals(otherSet.entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    public DatabaseEntrySet join(DatabaseEntrySet otherSet) {
        DatabaseEntrySet jointSet = new DatabaseEntrySet();
        jointSet.entries.addAll(this.entries);
        jointSet.entries.addAll(otherSet.entries);
        return jointSet;
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
