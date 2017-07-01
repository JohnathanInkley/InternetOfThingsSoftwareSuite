package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.DatabaseStuff.ClientDatabaseEditor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/api/users")
public class ChangeUserDetailsHandler {

    private static ClientDatabaseEditor editor;
    private static AuthenticationManager authenticationManager;
    private static Gson gson;

    public static void setClientDatabaseEditor(ClientDatabaseEditor clientDatabaseEditor) {
        editor = clientDatabaseEditor;
        authenticationManager = new AuthenticationManager(editor);
        AuthenticationFilter.setAuthenticationManager(authenticationManager);
        gson = new GsonBuilder().create();
    }


    @PUT
    @Secured
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getUpdatedUserDetailsAndAddToDatabase(String userAsJson, @Context SecurityContext securityContext) {
        UserJson modifiedUser = gson.fromJson(userAsJson, UserJson.class);
        String oldUsername = securityContext.getUserPrincipal().getName();
        UserEntry userEntry = editor.getUserEntry(oldUsername);
        modifiedUser.addNonVoidElementsToUserEntry(userEntry);
        editor.addUserEntry(userEntry);
        return Response.ok().build();
    }

    @PUT
    @Path("/updatePassword")
    @Secured
    @Consumes(MediaType.TEXT_PLAIN)
    public Response updatePassword(String oldPasswordAndNewPassword, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        UserEntry userEntry = editor.getUserEntry(username);
        OldAndNewPasswordPair newCredentials = gson.fromJson(oldPasswordAndNewPassword, OldAndNewPasswordPair.class);
        if (userEntry.validateCredentials(username, newCredentials.oldPassword)) {
            userEntry.setPasswordAndHash(newCredentials.newPassword);
            editor.addUserEntry(userEntry);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}

