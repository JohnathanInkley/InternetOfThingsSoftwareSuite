package BaseStationCode;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SensorReadingHandlerTest {

    @Test
    public void shouldAllowProducersToAddReadingsThatCanThenBeRemoved() {
        SensorReadingHandler underTest = new SensorReadingHandler();
        SensorReadingProducer producer = new SensorReadingProducer("owner.factory");
        producer.findPortAndOpen();

        underTest.add(producer.getSingleReading());
        underTest.add(producer.getSingleReading());
        underTest.get();
        String message = underTest.get();
        System.out.println(message);
        assertTrue(message.startsWith("["));
        assertTrue(message.endsWith("]"));

        producer.close();
    }
}