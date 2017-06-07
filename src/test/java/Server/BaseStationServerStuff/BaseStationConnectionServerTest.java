package Server.BaseStationServerStuff;

import BaseStationCode.DBEntryToStringConverter;
import BaseStationCode.SensorReadingSender;
import Server.DatabaseStuff.Database;
import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStationConnectionServerTest {

    @Test
    public void serverShouldRunAndReceiveReading() throws IOException, InterruptedException {
        String url = "http://localhost:8080/SensorServer";
        BaseStationConnectionServer underTest = new BaseStationConnectionServer(url);

        CountDownLatch messageReceived = new CountDownLatch(1);
        Database mockDB = mock(Database.class);
        when(mockDB.addEntry(any())).thenAnswer(invocation -> {
           Object[] args = invocation.getArguments();
           DatabaseEntry entry = (DatabaseEntry) args[0];
           assertEquals(entry.get("pet"), "dog");
           messageReceived.countDown();
           return new DatabaseEntry();
        });
        underTest.setDatabase(mockDB);

        IncomingReadingDecryptor decryptor = setUpSimpleDecryptor();
        underTest.setReadingDecryptor(decryptor);

        underTest.runServer();

        String entryString = generateStringRepresentingSimpleEntry();
        SensorReadingSender sender = new SensorReadingSender("http://localhost:8080/SensorServer/server");
        sender.send(entryString);

        assertTrue(messageReceived.await(10, TimeUnit.SECONDS));

        underTest.stopServer();
    }

    private IncomingReadingDecryptor setUpSimpleDecryptor() {
        IncomingReadingDecryptor decryptor = mock(IncomingReadingDecryptor.class);
        when(decryptor.decrypt(anyString())).thenAnswer(invocation -> invocation.getArguments()[0]);
        return decryptor;
    }

    private String generateStringRepresentingSimpleEntry() throws IOException {
        DatabaseEntry entry = new DatabaseEntry();
        entry.add("pet", "dog");
        return new DBEntryToStringConverter().convertToString(entry);
    }
}