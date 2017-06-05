package BaseStation;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

public class SensorReadingSender {

    private String serverURL;
    private HttpClient httpClient;

    public SensorReadingSender(String serverURL) {
        this.serverURL = serverURL;
        httpClient = HttpClients.createDefault();
    }

    public boolean send(String message) {
        try {
            HttpPost poster = new HttpPost(serverURL);
            poster.setEntity(new StringEntity(message));
            poster.setHeader("Content-Type", "text/plain");
            httpClient.execute(poster);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
