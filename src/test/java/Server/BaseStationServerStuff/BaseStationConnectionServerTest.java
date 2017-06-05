package Server.BaseStationServerStuff;

import BaseStation.ReadingEncryptor;
import BaseStation.SensorReadingSender;
import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStationConnectionServerTest {

    @Test
    public void serverShouldRunAndReceiveReading() throws IOException, InterruptedException {
        String url = "http://localhost:8080/SensorServer";
        BaseStationConnectionServer underTest = new BaseStationConnectionServer(url);

        CountDownLatch messageReceived = new CountDownLatch(1);
        SensorReadingParser parser = mock(SensorReadingParser.class);
        when(parser.parseReading(anyString())).thenAnswer(invocation -> {
           Object[] args = invocation.getArguments();
           assertEquals("[reading]", args[0]);
           messageReceived.countDown();
           return new DatabaseEntry();
        });

        underTest.setReadingParser(parser);

        ReadingEncryptor encryptor = new ReadingEncryptor();
        encryptor.readKey("src/main/java/BaseStation/AESKey.txt");
        underTest.setEncryptor(encryptor);

        underTest.runServer();

        SensorReadingSender sender = new SensorReadingSender("http://localhost:8080/SensorServer/server");
        sender.send(encryptor.encrypt("[reading]"));

        assertTrue(messageReceived.await(10, TimeUnit.SECONDS));

        underTest.stopServer();
    }


}