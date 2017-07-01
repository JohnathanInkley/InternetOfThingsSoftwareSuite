package Server.FrontEndServerStuff;

import Server.DatabaseStuff.Database;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationFilter;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationHandler;
import Server.FrontEndServerStuff.HttpResources.Authentication.ChangeUserDetailsHandler;
import Server.FrontEndServerStuff.HttpResources.Tests.ConnectionTest;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class FrontEndServer {

    private static final Set<Class<?>> httpResources = new HashSet<>();
    private String url;
    private Database database;
    private HttpServer server;

    public FrontEndServer(String url) {
        this.url = url;
        httpResources.add(ConnectionTest.class);
        httpResources.add(AuthenticationHandler.class);
        httpResources.add(ChangeUserDetailsHandler.class);
    }

    public void runServer() {
        URI baseUri = URI.create(url);
        ResourceConfig resourceConfig = new ResourceConfig(httpResources);
        resourceConfig.register(AuthenticationFilter.class);


        SSLContextConfigurator sslCon = new SSLContextConfigurator();
        sslCon.setKeyStoreFile("src/main/java/Server/FrontEndServerStuff/keystore_server");
        sslCon.setKeyStorePass("keypassword1");
        HttpHandler handler = ContainerFactory.createContainer(HttpHandler.class, resourceConfig);
        server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig, true,
                new SSLEngineConfigurator(sslCon, false, false, false));
    }

    public void stopServer() {
        server.shutdown();
    }

}
