package Server.FrontEndServerStuff;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.security.KeyStore;

public class HttpsClientMaker {

    public static HttpClient makeHttpsClient() {
        try {
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

            return httpClient;
        } catch (Exception e) {
            throw new RuntimeException("Couldn't make http client, check trust store and keystore files are there");
        }
    }
}
