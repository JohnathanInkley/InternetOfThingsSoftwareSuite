package Server.FrontEndServerStuff;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;

public class FrontEndServerConnectionTest {

    @Test
    public void serverShouldAllowStatusCheck() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException {
        String serverUrl = "https://localhost:8081/api/testConnection";
        FrontEndServer underTest = new FrontEndServer("https://localhost:8081");
        underTest.runServer();

        HttpClient httpClient = HttpsClientMaker.makeHttpsClient();
        HttpGet getter = new HttpGet(serverUrl);
        HttpResponse response = httpClient.execute(getter);
        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);

        assertEquals(200,response.getStatusLine().getStatusCode());
        assertEquals("connection ok", new String(messageBytes));

        underTest.stopServer();

    }


}