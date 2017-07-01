package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsernameAndPasswordFromLoginRequest(String usernameAndPassword) {
        UsernamePasswordPair credentials = generateCredentials(usernameAndPassword);
        if (authenticationManager.validateUser(credentials)) {
            UserJson userResponse = authenticationManager.generateAuthenticationResponse(credentials.username);
            String userAsJson = gson.toJson(userResponse);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(userAsJson)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    private UsernamePasswordPair generateCredentials(String usernameAndPassword) {
        return gson.fromJson(usernameAndPassword, UsernamePasswordPair.class);
    }

}
