package Server.BaseStationServerStuff;

import Server.DatabaseStuff.Database;
import Server.DatabaseStuff.DatabaseEntry;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/server")
public class PostResource {

    private static SensorReadingParser readingParser;
    private static Database database;
    private static IncomingReadingDecryptor readingDecryptor;

    public static void setReadingParser(SensorReadingParser parser) {
        readingParser = parser;
    }

    public static void setDatabase(Database db) {
        database = db;
    }

    public static void setReadingEncryptor(IncomingReadingDecryptor encryptor) {
        readingDecryptor = encryptor;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public void parseMessageAndAddToDatabase(String encodedMessage) {
        String reading = readingDecryptor.decrypt(encodedMessage);
        if (!reading.equals(IncomingReadingDecryptor.UNAUTHORIZED_MESSAGE_ATTEMPT)) {
            DatabaseEntry entry = readingParser.parseReading(reading);
            addToDatabase(entry);
        }
    }

    private void addToDatabase(DatabaseEntry entry) {
        System.out.println("adding entry...");
        database.addEntry(entry);
    }

}
