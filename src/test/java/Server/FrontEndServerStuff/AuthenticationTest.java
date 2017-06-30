package Server.FrontEndServerStuff;

import Server.AccountManagement.UsernamePasswordPair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AuthenticationTest {

    private static FrontEndServer underTest;
    private static HttpClient httpClient;
    private static GsonBuilder gsonBuilder;
    private static String serverUrl;

    @BeforeClass
    public static void setUp() {
        serverUrl = "https://localhost:8081";
        underTest = new FrontEndServer(serverUrl);
        underTest.runServer();
        httpClient = HttpsClientMaker.makeHttpsClient();
        gsonBuilder = new GsonBuilder();
    }

    @AfterClass
    public static void cleanUp() {
        underTest.stopServer();
    }


    @Test
    public void serverShouldReceiveCredentialsAndPassToHandler() throws IOException {
        String apiAddress = serverUrl + "/api/authenticate";

        Gson gson = gsonBuilder.create();
        String credentials = gson.toJson(new UsernamePasswordPair("u", "p"));
        HttpPost post = new HttpPost(apiAddress);
        post.setHeader("Content-Type", "text/plain");
        post.setEntity(new StringEntity(credentials));
        HttpResponse response = httpClient.execute(post);

        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("fakeToken for u and p", new String(messageBytes));


    }



}