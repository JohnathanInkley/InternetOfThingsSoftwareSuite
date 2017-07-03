package Server.FrontEndServerStuff.HttpResources.Sites;

import Server.AccountManagement.UserEntry;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.FrontEndServerStuff.FrontEndServer;
import Server.FrontEndServerStuff.HttpsClientMaker;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManagerTest.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetListOfSitesHandlerTest {

    private static List<String> LIST_OF_SITES;
    private static List<String> CLIENT_LIST_OF_SITES;
    public static String ADMIN_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwidXNlcm5hbWUiOiJhZG1pbiIsImNsaWVudCI6Im93bmVyIn0.czJJRsWOn-6cNTs0PEW4NBHnYd_Ew1laeXq-EryHCXg";
    public static String ADMIN_USERNAME = "admin";

    private static String serverUrl;
    private static FrontEndServer underTest;
    private static HttpClient httpClient;
    private static ClientDatabaseEditor editor;
    private static UserEntry user;
    private static UserEntry admin;
    private Gson gson;

    @BeforeClass
    public static void classSetUp() {
        serverUrl = "https://localhost:8081";
        underTest = new FrontEndServer(serverUrl);
        underTest.runServer();
    }

    @Before
    public void setUpMockObjectsCleanEachTime() {
        httpClient = HttpsClientMaker.makeHttpsClient();
        gson = new GsonBuilder().create();
        setUpMockObjects();
    }

    @AfterClass
    public static void cleanUp() {
        underTest.stopServer();
    }

    @Test
    public void serverShouldReturnListOfSitesUserHasAccessTo() throws IOException {
        HttpResponse response = getSiteListForUser();
        List<String> listOfSites = getListOfSitesFromResponse(response);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(LIST_OF_SITES, listOfSites);
    }

    @Test
    public void adminShouldBeAbleToSetSitePermissionsForUser() throws IOException {
        HttpResponse response = submitNewSiteForUserUsingToken(ADMIN_TOKEN, CLIENT_LIST_OF_SITES.get(2));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

        HttpResponse userResponse = getSiteListForUser();
        List<String> listFromServer = getListOfSitesFromResponse(userResponse);

        assertEquals(CLIENT_LIST_OF_SITES, listFromServer);
    }

    @Test
    public void adminShouldNotBeAbleToAddSiteIfSiteNotBelongToClient() throws IOException {
        HttpResponse response = submitNewSiteForUserUsingToken(ADMIN_TOKEN, "NOT_MY_SITE");

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void nonAdminsShouldNotBeAbleToAddSitesForAnyone() throws IOException {
        HttpResponse response = submitNewSiteForUserUsingToken(NON_ADMIN_TOKEN, CLIENT_LIST_OF_SITES.get(2));

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    private List<String> getListOfSitesFromResponse(HttpResponse userResponse) throws IOException {
        byte[] messageBytes = new byte[(int) userResponse.getEntity().getContentLength()];
        userResponse.getEntity().getContent().read(messageBytes);
        Type listOfStringsType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(new String(messageBytes), listOfStringsType);
    }

    private HttpResponse getSiteListForUser() throws IOException {
        HttpGet checkSitesUpdated = new HttpGet(serverUrl + "/api/sites");
        checkSitesUpdated.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        return httpClient.execute(checkSitesUpdated);
    }

    private HttpResponse submitNewSiteForUserUsingToken(String token, String siteName) throws IOException {
        HttpPut putNewSite = new HttpPut(serverUrl + "/api/sites/add");
        putNewSite.setHeader("Authorization", "Bearer " + token);
        putNewSite.setHeader("Content-Type", MediaType.APPLICATION_JSON);
        String usernameSitePair = gson.toJson(new UsernameSitePair(VALID_USERNAME, siteName));
        putNewSite.setEntity(new StringEntity(usernameSitePair));
        return httpClient.execute(putNewSite);
    }

    private static void setUpMockObjects() {
        editor = mock(ClientDatabaseEditor.class);
        makeListOfSites();
        user = UserEntry.generateUnbuiltUser(CLIENT);
        user.setId(1);
        user.generateDefaultPasswordAndBuild();
        user.setUsername(VALID_USERNAME);
        user.giveSitePermission(LIST_OF_SITES.get(0));
        user.giveSitePermission(LIST_OF_SITES.get(1));
        admin = UserEntry.generateUnbuiltUser(CLIENT);
        admin.setAdminFlag();
        admin.setId(2);
        admin.generateDefaultPasswordAndBuild();
        admin.setUsername(ADMIN_USERNAME);
        when(editor.getUserEntry(VALID_USERNAME)).thenReturn(user);
        when(editor.getUserEntry(ADMIN_USERNAME)).thenReturn(admin);
        when(editor.getSiteNamesForClient(CLIENT)).thenReturn(CLIENT_LIST_OF_SITES);
        GetListOfSitesHandler.setClientDatabaseEditor(editor);

    }

    private static void makeListOfSites() {
        LIST_OF_SITES = new ArrayList<>();
        LIST_OF_SITES.add("site1");
        LIST_OF_SITES.add("site2");
        CLIENT_LIST_OF_SITES = new ArrayList<>(LIST_OF_SITES);
        CLIENT_LIST_OF_SITES.add("site3");
    }

}