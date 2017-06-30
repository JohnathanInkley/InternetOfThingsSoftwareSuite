package Server.FrontEndServerStuff;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;

public class FrontEndServerConnectionTest {

    @Test
    public void serverShouldAllowStatusCheck() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException, CertificateException, UnrecoverableKeyException {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        String serverUrl = "https://localhost:8081/api/testConnection";
        FrontEndServer underTest = new FrontEndServer("https://localhost:8081");
        underTest.runServer();


        char[] password = "keypassword1".toCharArray();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream("src/main/java/Server/FrontEndServerStuff/myTrustStore"), password);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream("src/main/java/Server/FrontEndServerStuff/keystore_server"), password);

        SSLContext context = SSLContexts.custom()
                .loadKeyMaterial(keyStore, password)
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(context,
                new String[]{"TLSv1.2", "TLSv1.1"},
                null,
                new NoopHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        HttpGet getter = new HttpGet(serverUrl);
        HttpResponse response = httpClient.execute(getter);
        byte[] messageBytes = new byte[(int) response.getEntity().getContentLength()];
        response.getEntity().getContent().read(messageBytes);

        assertEquals(200,response.getStatusLine().getStatusCode());
        assertEquals("connection ok", new String(messageBytes));

        underTest.stopServer();

    }


}