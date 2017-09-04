package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.DatabaseStuff.ClientDatabaseEditor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("")
public class UsersAndDetailsHandler {

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
    @Path("/api/users")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUpdatedUserDetailsAndAddToDatabase(String userAsJson, @Context SecurityContext securityContext) {
        UserEntry userEntry = getOriginalUserFromDatabase(securityContext);
        parseUserChangesAndUpdateDatabase(userAsJson, userEntry);
        String newUserAsJson = generateNewUserTokenAndResponse(userEntry);
        return Response.status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(newUserAsJson)
                .build();
    }

    private String generateNewUserTokenAndResponse(UserEntry userEntry) {
        UserJson newUserResponse = authenticationManager.generateAuthenticationResponse(userEntry.getUsername());
        return gson.toJson(newUserResponse);
    }

    private void parseUserChangesAndUpdateDatabase(String userAsJson, UserEntry userEntry) {
        UserJson modifiedUser = gson.fromJson(userAsJson, UserJson.class);
        modifiedUser.addNonVoidElementsToUserEntry(userEntry);
        editor.addUserEntry(userEntry);
    }

    private UserEntry getOriginalUserFromDatabase(@Context SecurityContext securityContext) {
        String oldUsername = securityContext.getUserPrincipal().getName();
        return editor.getUserEntry(oldUsername);
    }

    @PUT
    @Path("/api/users/updatePassword")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(String oldPasswordAndNewPassword, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        UserEntry userEntry = editor.getUserEntry(username);
        OldAndNewPasswordPair newCredentials = gson.fromJson(oldPasswordAndNewPassword, OldAndNewPasswordPair.class);
        if (userEntry.validateCredentials(username, newCredentials.oldPassword)) {
            userEntry.setPasswordAndHash(newCredentials.newPassword);
            editor.addUserEntry(userEntry);
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity("{\"Incorrect Password\"}").build();
        }
    }

    @GET
    @Path("/api/users/userList")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getListOfUsers(@Context SecurityContext securityContext) {
        String adminUsername = securityContext.getUserPrincipal().getName();
        UserEntry adminEntry = editor.getUserEntry(adminUsername);
        if (adminEntry.isAdmin()) {
            List<String> userList = editor.getUserNamesForClient(adminEntry.getClientName());
            String userListJson = gson.toJson(userList);
            return Response
                    .status(Response.Status.OK)
                    .entity(userListJson)
                    .build();
        } else {
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .build();
        }
    }
}

