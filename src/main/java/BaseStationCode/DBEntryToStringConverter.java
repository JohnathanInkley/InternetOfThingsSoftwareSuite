package BaseStationCode;

import Server.DatabaseStuff.DatabaseEntry;

import java.io.*;
import java.util.Base64;

public class DBEntryToStringConverter {
    private ByteArrayOutputStream byteOutputStream;
    private ByteArrayInputStream byteInputStream;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;


    public String convertToString(DatabaseEntry entry) throws IOException {
        try {
            byteOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteOutputStream);
            objectOutputStream.writeObject(entry);
            return new String(Base64.getEncoder().encode(byteOutputStream.toByteArray()));
        } finally {
            closeOutputStreams();
        }
    }

    private void closeOutputStreams() throws IOException {
        byteOutputStream.close();
        objectOutputStream.close();
    }

    public DatabaseEntry convertToEntry(String entryAsString) throws IOException {
        try {
            byteInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(entryAsString));
            objectInputStream = new ObjectInputStream(byteInputStream);
            return (DatabaseEntry) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("DatabaseEntry class cannot be found, code must have been modified" +
                    "and import statement removed, which should break convertToString function too");
        } finally {
            closeInputStreams();
        }
    }

    private void closeInputStreams() throws IOException {
        byteInputStream.close();
        if (objectInputStream != null) {
            objectInputStream.close();
        }
    }
}
