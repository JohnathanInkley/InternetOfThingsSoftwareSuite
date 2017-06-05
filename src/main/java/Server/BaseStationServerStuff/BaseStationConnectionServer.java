package Server.BaseStationServerStuff;

import BaseStation.ReadingEncryptor;
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
    private ReadingEncryptor encryptor;

    public BaseStationConnectionServer(String url) {
        this.url = url;
    }

    public void runServer() {
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

    public void setEncryptor(ReadingEncryptor encryptor) {
        this.encryptor = encryptor;
        PostResource.setReadingEncryptor(encryptor);
    }
}
