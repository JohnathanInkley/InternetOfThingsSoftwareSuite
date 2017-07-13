package Server.BaseStationServerStuff;

import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.DatabaseStuff.Database;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class BaseStationConnectionServer {

    private String url;
    private SensorReadingParser readingParser;
    private Database database;
    private HttpServer server;
    private IncomingReadingDecryptor decryptor;

    public static void main(String[] args) {
        // Create database
        Database tsDatabase = new Database("ts", "http://0.0.0.0:8086/");
        Database clientDatabase = new Database("ClientManagementDatabase", "http://0.0.0.0:8086/");

        // Set up client connection server
        BaseStationConnectionServer server = new BaseStationConnectionServer("http://0.0.0.0:8080/SensorServer");
        SensorReadingParser parser = new SensorReadingParser();
        server.setReadingParser(parser);

        IncomingReadingDecryptor decryptor = new IncomingReadingDecryptor();
        ClientDatabaseEditor editor = new ClientDatabaseEditor(clientDatabase);
        decryptor.setClientDatabaseEditor(editor);
        server.setReadingDecryptor(decryptor);

        server.setDatabase(tsDatabase);

        // run server
        server.runServer();
    }

    public BaseStationConnectionServer(String url) {
        this.url = url;
    }

    public void runServer() {
        PostResource.initialiseWebService();
        URI baseUri = URI.create(url);
        ResourceConfig resourceConfig = new ResourceConfig(PostResource.class);
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);
    }

    public void stopServer() {
        server.shutdown();
    }

    public void setReadingParser(SensorReadingParser readingParser) {
        this.readingParser = readingParser;
        PostResource.setReadingParser(readingParser);
    }

    public void setDatabase(Database database) {
        this.database = database;
        PostResource.setDatabase(database);
    }

    public void setReadingDecryptor(IncomingReadingDecryptor decryptor) {
        this.decryptor = decryptor;
        PostResource.setReadingEncryptor(this.decryptor);
    }

}
