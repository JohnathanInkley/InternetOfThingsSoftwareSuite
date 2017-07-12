package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/authenticate")
public class AuthenticationHandler {

    private static ClientDatabaseEditor editor;
    private static AuthenticationManager authenticationManager;
    private static Gson gson;

    public static void setClientDatabaseEditor(ClientDatabaseEditor clientDatabaseEditor) {
        editor = clientDatabaseEditor;
        authenticationManager = new AuthenticationManager(editor);
        gson = new GsonBuilder().create();
    }

    @GET
    @Secured
    @Path("/foo")
    @Produces(MediaType.TEXT_PLAIN)
    public Response helloWorld() {
        return Response.status(Response.Status.OK).type(MediaType.TEXT_PLAIN).entity("Hello").build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameAndPasswordFromLoginRequest(String usernameAndPassword) {
        System.out.println("logging in...");
        UsernamePasswordPair credentials = generateCredentials(usernameAndPassword);
        if (authenticationManager.validateUser(credentials)) {
            UserJson userResponse = authenticationManager.generateAuthenticationResponse(credentials.username);
            String userAsJson = gson.toJson(userResponse);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(userAsJson)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Bad credentials").build();
        }
    }

    private UsernamePasswordPair generateCredentials(String usernameAndPassword) {
        return gson.fromJson(usernameAndPassword, UsernamePasswordPair.class);
    }

}
