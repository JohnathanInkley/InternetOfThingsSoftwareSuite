package Server.BaseStationServerStuff;

import BaseStationCode.DBEntryToStringConverter;
import Server.DatabaseStuff.Database;
import Server.DatabaseStuff.DatabaseEntry;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/server")
public class PostResource {

    private static SensorReadingParser readingParser;
    private static Database database;
    private static IncomingReadingDecryptor readingDecryptor;
    private static DBEntryToStringConverter entryConverter;

    public static void setReadingParser(SensorReadingParser parser) {
        readingParser = parser;
    }

    public static void setDatabase(Database db) {
        database = db;
    }

    public static void setReadingEncryptor(IncomingReadingDecryptor encryptor) {
        readingDecryptor = encryptor;
    }

    public static void initialiseWebService() {
        entryConverter = new DBEntryToStringConverter();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response parseMessageAndAddToDatabase(String encodedMessage) {
        String entryAsString = readingDecryptor.decrypt(encodedMessage);
        if (!entryAsString.equals(IncomingReadingDecryptor.UNAUTHORIZED_MESSAGE_ATTEMPT)) {
            try {
                DatabaseEntry entry = entryConverter.convertToEntry(entryAsString);
                addToDatabase(entry);
                return Response.status(Response.Status.OK).build();
            } catch (IOException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private void addToDatabase(DatabaseEntry entry) {
        database.addEntry(entry);
    }

}
