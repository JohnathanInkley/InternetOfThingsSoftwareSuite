package Server.FrontEndServerStuff.HttpResources.Sites;

import Server.AccountManagement.UserEntry;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationFilter;
import Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManager;
import Server.FrontEndServerStuff.HttpResources.Authentication.Secured;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("")
public class GetListOfSitesHandler {

    private static ClientDatabaseEditor editor;
    private static AuthenticationManager authenticationManager;
    private static Gson gson;

    public static void setClientDatabaseEditor(ClientDatabaseEditor clientDatabaseEditor) {
        editor = clientDatabaseEditor;
        authenticationManager = new AuthenticationManager(editor);
        AuthenticationFilter.setAuthenticationManager(authenticationManager);
        gson = new GsonBuilder().create();
    }

    @GET
    @Path("/api/sites")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSitesUserHasAccessTo(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        UserEntry user = editor.getUserEntry(username);
        List<String> sitePermissions = user.getSitePermissions();
        String sitePermissionsAsString = gson.toJson(sitePermissions);
        return Response.status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(sitePermissionsAsString)
                .build();
    }

    @PUT
    @Path("/api/sites/add")
    @Secured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addSitePermissionsForUser(String userSiteString, @Context SecurityContext securityContext) {
        String adminUsername = securityContext.getUserPrincipal().getName();
        UserEntry adminEntry = editor.getUserEntry(adminUsername);

        UsernameSitePair usernameSitePair = gson.fromJson(userSiteString, UsernameSitePair.class);
        List<String> clientSites = editor.getSiteNamesForClient(adminEntry.getClientName());

        if (adminEntry.isAdmin() && clientSites.contains(usernameSitePair.siteName)) {
            UserEntry userEntry = editor.getUserEntry(usernameSitePair.username);
            userEntry.giveSitePermission(usernameSitePair.siteName);
            editor.addUserEntry(userEntry);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
