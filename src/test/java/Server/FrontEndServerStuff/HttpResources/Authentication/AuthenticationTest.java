package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.FrontEndServerStuff.FrontEndServer;
import Server.FrontEndServerStuff.HttpsClientMaker;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManagerTest.*;
import static Server.FrontEndServerStuff.HttpResources.Sites.GetListOfSitesHandlerTest.ADMIN_TOKEN;
import static Server.FrontEndServerStuff.HttpResources.Sites.GetListOfSitesHandlerTest.ADMIN_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationTest {

    private static FrontEndServer underTest;
    private static HttpClient httpClient;
    private static Gson gson;
    private static String serverUrl;
    private static String apiAddress;
    private static UserEntry user;
    private static UserEntry admin;
    private static List<String> userList = new ArrayList<>(Arrays.asList(VALID_USERNAME, ADMIN_USERNAME));

    @BeforeClass
    public static void classSetUp() {
        serverUrl = "https://localhost:8081";
        apiAddress = serverUrl + "/api/authenticate";
        underTest = new FrontEndServer(serverUrl);
        underTest.runServer();
    }

    @Before
    public void setUp() {
        httpClient = HttpsClientMaker.makeHttpsClient();
        gson = new GsonBuilder().create();
        setMockClientDatabaseEditor();
    }

    @AfterClass
    public static void cleanUp() {
        underTest.stopServer();
    }

    @Test
    public void serverShouldGenerateTokenIfCredentialsValid() throws IOException {
        HttpResponse response = submitCredentialsToServer(VALID_USERNAME, VALID_PASSWORD);
        UserJson user = getUserJsonFromResponse(response);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(NON_ADMIN_TOKEN, user.token);
        assertEquals(EMAIL, user.email);
        assertEquals(FIRST_NAME, user.firstName);
        assertEquals(LAST_NAME, user.lastName);
        assertEquals(CLIENT, user.client);
        assertEquals(USER_ID, user.id);
        assertEquals(VALID_USERNAME, user.username);
    }

    @Test
    public void serverShouldReturn401IfBadCredentialsGiven() throws IOException {
        HttpResponse response = submitCredentialsToServer(VALID_USERNAME, INVALID_PASSWORD);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void serverShouldRejectRequestWithInvalidJWT() throws IOException {
        HttpPut put = new HttpPut(serverUrl + "/api/users");
        put.setHeader("Authorization", "Bearer " + BAD_TOKEN);
        put.setEntity(new StringEntity(""));
        HttpResponse response = httpClient.execute(put);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void serverShouldAcceptUserUpdatesWithValidJWT() throws IOException {
        HttpResponse getUserResponse = submitCredentialsToServer(VALID_USERNAME, VALID_PASSWORD);
        UserJson modifiedUser = getUserJsonFromResponse(getUserResponse);
        modifiedUser.firstName = "newFirstName";
        modifiedUser.username = NEW_USERNAME;

        HttpPut put = new HttpPut(serverUrl + "/api/users");
        put.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        put.setEntity(new StringEntity(gson.toJson(modifiedUser,UserJson.class)));
        HttpResponse response = httpClient.execute(put);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(user.getFirstName(), "newFirstName");
        assertEquals(user.getUsername(), NEW_USERNAME);

        UserJson userJson = getUserJsonFromResponse(response);
        String newToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyIiwidXNlcm5hbWUiOiJuZXdVc2VyIiwiY2xpZW50Ijoib3duZXIifQ.hCwR8ZX7quuyKKxyLJHHQ3KOBiM1n93lQls5GcliDgs";
        assertEquals(newToken, userJson.token);
    }

    private UserJson getUserJsonFromResponse(HttpResponse getUserResponse) throws IOException {
        byte[] messageBytes = new byte[(int) getUserResponse.getEntity().getContentLength()];
        getUserResponse.getEntity().getContent().read(messageBytes);
        return gson.fromJson(new String(messageBytes), UserJson.class);
    }

    @Test
    public void shouldGive200IfPasswordChangedAndValidOldPasswordSubmitted() throws IOException {
        OldAndNewPasswordPair updatesToPassword = new OldAndNewPasswordPair(VALID_PASSWORD, "newPass");
        String body = gson.toJson(updatesToPassword);

        HttpResponse response = attemptToChangePassword(body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void shouldGive401IfPasswordChangeAttemptMadeButOldPasswordInvalid() throws IOException {
        OldAndNewPasswordPair updatesToPassword = new OldAndNewPasswordPair(INVALID_PASSWORD, "newPass");
        String body = gson.toJson(updatesToPassword);

        HttpResponse response = attemptToChangePassword(body);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void adminsShouldBeAbleToSeeAllAccountsBelongingToTheirClient() throws IOException {
        HttpGet getUsers = new HttpGet(serverUrl + "/api/users/userList");
        getUsers.setHeader("Authorization", "Bearer " + ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getUsers);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        Type listOfStringsType = new TypeToken<List<String>>() {}.getType();
        List<String> listOfUsers = gson.fromJson(new String(messageBytes), listOfStringsType);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(userList, listOfUsers);
    }

    @Test
    public void nonAdminsShouldNotBeAbleToSeeAllAccounts() throws IOException {
        HttpGet getUsers = new HttpGet(serverUrl + "/api/users/userList");
        getUsers.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        HttpResponse response = httpClient.execute(getUsers);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    private HttpResponse attemptToChangePassword(String body) throws IOException {
        HttpPut put = new HttpPut(serverUrl + "/api/users/updatePassword");
        put.setHeader("Authorization", "Bearer " + NON_ADMIN_TOKEN);
        put.setEntity(new StringEntity(body));
        return httpClient.execute(put);
    }

    private HttpResponse submitCredentialsToServer(String username, String password) throws IOException {
        String credentials = gson.toJson(new UsernamePasswordPair(username, password));
        HttpPost post = new HttpPost(apiAddress);
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity(credentials));
        return httpClient.execute(post);
    }

    public static void setMockClientDatabaseEditor() {
        ClientDatabaseEditor editor = mock(ClientDatabaseEditor.class);
        user = UserEntry.generateUnbuiltUser(CLIENT);
        user.setId(USER_ID);
        user.generateDefaultPasswordAndBuild();
        user.setPasswordAndHash(VALID_PASSWORD);
        user.setLastName(LAST_NAME);
        user.setFirstName(FIRST_NAME);
        user.setUsername(VALID_USERNAME);
        user.setEmail(EMAIL);
        admin = UserEntry.generateUnbuiltUser(CLIENT);
        admin.setAdminFlag();
        admin.setId(2);
        admin.generateDefaultPasswordAndBuild();
        admin.setUsername(ADMIN_USERNAME);
        when(editor.getUserEntry(VALID_USERNAME)).thenReturn(user);
        when(editor.getUserEntry(NEW_USERNAME)).thenReturn(user);
        when(editor.getUserEntry(ADMIN_USERNAME)).thenReturn(admin);
        when(editor.getUserNamesForClient(CLIENT)).thenReturn(userList);
        setClientDatabaseEditorsInHandlers(editor);
    }

    private static void setClientDatabaseEditorsInHandlers(ClientDatabaseEditor editor) {
        AuthenticationHandler.setClientDatabaseEditor(editor);
        ChangeUserDetailsHandler.setClientDatabaseEditor(editor);
    }

}