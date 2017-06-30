package Server.FrontEndServerStuff.HttpResources.Tests;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/testConnection")
public class ConnectionTest {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String postRequestHandler(@Context HttpHeaders headers) {
        Response.ok("success", MediaType.TEXT_PLAIN);
        return "connection ok";
    }
}
