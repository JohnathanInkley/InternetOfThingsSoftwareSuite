package Server.BaseStationServerStuff;

import BaseStation.ReadingEncryptor;
import Server.DatabaseStuff.Database;
import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DeviceCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/server")
public class PostResource {

    private static SensorReadingParser readingParser;
    private static Database database;
    private static ReadingEncryptor readingEncryptor;

    public static void setReadingParser(SensorReadingParser parser) {
        readingParser = parser;
    }

    public static void setDatabase(Database db) {
        database = db;
    }

    public static void setReadingEncryptor(ReadingEncryptor encryptor) {
        readingEncryptor = encryptor;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void parseByteMessageAndAddToDatabase(String encodedMessage) {
        String reading = readingEncryptor.decrypt(encodedMessage);
        DatabaseEntry entry = readingParser.parseReading(reading);
        if (database != null) {
            addToDatabase(entry);
        }
    }

    private void addToDatabase(DatabaseEntry entry) {
        System.out.println("adding entry...");
        DeviceCollection dc = new DeviceCollection("owner", "factory");
        entry.setDeviceCollectionIdentifier(dc);
        database.addEntry(entry);
    }

}
