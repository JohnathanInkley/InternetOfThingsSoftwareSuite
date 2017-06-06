package BaseStationCode;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class SensorReadingSender {

    private static int NUMBER_OF_RETRIES_BEFORE_FAILURE = 14; // Roughly 30 minutes
    private static int EXPONENTIAL_BACK_OFF_MULTIPLIER_IN_MS = 125;

    private String serverURL;
    private HttpClient httpClient;

    private String currentMessage;
    private boolean hasCurrentMessageBeenSent;
    private int retryCount;
    private int backOffTime;

    public SensorReadingSender(String serverURL) {
        this.serverURL = serverURL;
        httpClient = HttpClients.createDefault();
    }

    public boolean send(String message) {
        initialiseCurrentMessageSentParameters(message);
        while (!Thread.currentThread().isInterrupted() && !hasCurrentMessageBeenSent && retryCount < NUMBER_OF_RETRIES_BEFORE_FAILURE) {
            try {
                tryAndSendCurrentMessage();
            } catch (IOException e) {
                waitWithExponentialBackOff();
            }
        }
        return hasCurrentMessageBeenSent;
    }

    private void initialiseCurrentMessageSentParameters(String message) {
        currentMessage = message;
        retryCount = 0;
        backOffTime = EXPONENTIAL_BACK_OFF_MULTIPLIER_IN_MS;
        hasCurrentMessageBeenSent = false;
    }

    private void tryAndSendCurrentMessage() throws IOException {
        HttpPost poster = new HttpPost(serverURL);
        poster.setEntity(new StringEntity(currentMessage));
        poster.setHeader("Content-Type", "text/plain");
        httpClient.execute(poster);
        hasCurrentMessageBeenSent = true;
    }

    private void waitWithExponentialBackOff() {
        try {
            Thread.sleep(backOffTime);
            backOffTime *= 2;
            retryCount++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
