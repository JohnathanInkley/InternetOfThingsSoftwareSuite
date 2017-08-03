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
    @Path("/api/{usernameToView}/sites")
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSitesUserHasAccessTo(@PathParam("usernameToView") String usernameToView, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        UserEntry user = editor.getUserEntry(username);
        UserEntry userToView = editor.getUserEntry(usernameToView);

        if (userToView == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (username.equals(usernameToView) || (user.isAdmin() && user.getClientName().equals(userToView.getClientName()))) {
            List<String> sitePermissions = userToView.getSitePermissions();
            String sitePermissionsAsString = gson.toJson(sitePermissions);
            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(sitePermissionsAsString)
                    .build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @PUT
    @Path("/api/sites/{siteName}/addUser/{username}")
    @Secured
    public Response addSitePermissionsForUser(@PathParam("siteName") String siteName,
                                              @PathParam("username") String username,
                                              @Context SecurityContext securityContext) {
        String adminUsername = securityContext.getUserPrincipal().getName();
        UserEntry adminEntry = editor.getUserEntry(adminUsername);

        List<String> clientSites = editor.getSiteNamesForClient(adminEntry.getClientName());

        if (adminEntry.isAdmin() && clientSites.contains(siteName)) {
            UserEntry userEntry = editor.getUserEntry(username);
            userEntry.giveSitePermission(siteName);
            editor.addUserEntry(userEntry);
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @PUT
    @Path("/api/sites/{siteName}/removeUser/{username}")
    @Secured
    public Response removeSitePermissionsForUser(@PathParam("siteName") String siteName,
                                              @PathParam("username") String username,
                                              @Context SecurityContext securityContext) {
        String adminUsername = securityContext.getUserPrincipal().getName();
        UserEntry adminEntry = editor.getUserEntry(adminUsername);

        List<String> clientSites = editor.getSiteNamesForClient(adminEntry.getClientName());

        if (adminEntry.isAdmin() && clientSites.contains(siteName)) {
            UserEntry userEntry = editor.getUserEntry(username);
            System.out.println("before: " + userEntry.getSitePermissions());
            userEntry.removeSitePermission(siteName);
            System.out.println("supposedly after: " + userEntry.getSitePermissions());
            editor.addUserEntry(userEntry);
            userEntry = editor.getUserEntry(username);
            System.out.println("real after: " + userEntry.getSitePermissions());
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
