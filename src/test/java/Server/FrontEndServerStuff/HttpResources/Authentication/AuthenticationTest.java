package Server.FrontEndServerStuff.HttpResources.Authentication;

import Server.AccountManagement.UserEntry;
import Server.AccountManagement.UsernamePasswordPair;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.FrontEndServerStuff.FrontEndServer;
import Server.FrontEndServerStuff.HttpsClientMaker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static Server.FrontEndServerStuff.HttpResources.Authentication.AuthenticationManagerTest.*;
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

    @BeforeClass
    public static void setUp() {
        serverUrl = "https://localhost:8081";
        apiAddress = serverUrl + "/api/authenticate";
        underTest = new FrontEndServer(serverUrl);
        underTest.runServer();
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
        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);
        UserJson user = gson.fromJson(new String(messageBytes), UserJson.class);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(VALID_TOKEN, user.token);
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
        byte[] messageBytes = new byte[(int) getUserResponse.getEntity().getContentLength()];
        getUserResponse.getEntity().getContent().read(messageBytes);
        UserJson modifiedUser = gson.fromJson(new String(messageBytes), UserJson.class);
        modifiedUser.firstName = "newFirstName";
        modifiedUser.lastName = "newLastName";

        HttpPut put = new HttpPut(serverUrl + "/api/users");
        put.setHeader("Authorization", "Bearer " + VALID_TOKEN);
        put.setEntity(new StringEntity(gson.toJson(modifiedUser,UserJson.class)));
        HttpResponse response = httpClient.execute(put);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
        assertEquals(user.getFirstName(), "newFirstName");
        assertEquals(user.getLastName(), "newLastName");

        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
    }

    @Test
    public void shouldGive200IfPasswordChangedAndValidOldPasswordSubmitted() throws IOException {
        OldAndNewPasswordPair updatesToPassword = new OldAndNewPasswordPair(VALID_PASSWORD, "newPass");
        String body = gson.toJson(updatesToPassword);

        HttpResponse response = attemptToChangePassword(body);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());

        user.setPasswordAndHash(VALID_PASSWORD);
    }

    @Test
    public void shouldGive401IfPasswordChangeAttemptMadeButOldPasswordInvalid() throws IOException {
        OldAndNewPasswordPair updatesToPassword = new OldAndNewPasswordPair(INVALID_PASSWORD, "newPass");
        String body = gson.toJson(updatesToPassword);

        HttpResponse response = attemptToChangePassword(body);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatusLine().getStatusCode());

        user.setPasswordAndHash(VALID_PASSWORD);
    }

    private HttpResponse attemptToChangePassword(String body) throws IOException {
        HttpPut put = new HttpPut(serverUrl + "/api/users/updatePassword");
        put.setHeader("Authorization", "Bearer " + VALID_TOKEN);
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

    private static void setMockClientDatabaseEditor() {
        ClientDatabaseEditor editor = mock(ClientDatabaseEditor.class);
        user = UserEntry.generateUnbuiltUser(CLIENT);
        user.setId(USER_ID);
        user.generateDefaultPasswordAndBuild();
        user.setPasswordAndHash(VALID_PASSWORD);
        user.setLastName(LAST_NAME);
        user.setFirstName(FIRST_NAME);
        user.setUserName(VALID_USERNAME);
        user.setEmail(EMAIL);
        when(editor.getUserEntry(VALID_USERNAME)).thenReturn(user);
        setClientDatabaseEditorsInHandlers(editor);
    }

    private static void setClientDatabaseEditorsInHandlers(ClientDatabaseEditor editor) {
        AuthenticationHandler.setClientDatabaseEditor(editor);
        ChangeUserDetailsHandler.setClientDatabaseEditor(editor);
    }

}