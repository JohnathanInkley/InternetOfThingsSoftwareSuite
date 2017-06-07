package BaseStationCode;

import Server.BaseStationServerStuff.SensorReadingParser;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseStationManagerTest {

    @Test
    public void shouldTakeProducerAndSenderAndAHandlerAndAllowToWork() throws InterruptedException {
        BaseStationManager underTest = new BaseStationManager();

        ReadingEncryptor encryptor = mock(ReadingEncryptor.class);
        when(encryptor.encrypt(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return args[0];
        });

        SensorReadingProducer producer = mock(SensorReadingProducer.class);
        when(producer.getSingleReading()).thenReturn("[{IP,,123}]");

        SensorReadingSender sender = mock(SensorReadingSender.class);
        CountDownLatch checkMessagesAreProcessed = new CountDownLatch(100);
        when(sender.send(anyString())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            String message = (String) args[0];
            assertTrue(message.startsWith("owner.factory!"));
            checkMessagesAreProcessed.countDown();
            return true;
        });

        underTest.setBackupCreator(new SensorReadingBackupCreator("testBackup"));
        underTest.setHandler(new SensorReadingHandler(), 10);
        underTest.setProducer(producer);
        underTest.setConsumer(sender);
        underTest.setEncryptor(encryptor);
        underTest.setReadingParser(new SensorReadingParser());
        underTest.setDeviceCollection("owner.factory");
        underTest.setEntryToStringConverter(new DBEntryToStringConverter());

        underTest.start();
        assertTrue(checkMessagesAreProcessed.await(20, TimeUnit.SECONDS));
        underTest.join();
    }


}