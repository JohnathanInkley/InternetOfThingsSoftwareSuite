package BaseStationCode;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class SensorReadingProducerTest {

    @Test
    public void shouldBeAbleToFindSerialPortDeviceFile() {
        SensorReadingProducer underTest  = new SensorReadingProducer("owner.factory");
        underTest.findPortAndOpen();
        assertEquals("/dev/cu.usbmodemL3002981", underTest.getSerialPortPath());
        underTest.close();
    }

    @Test
    public void shouldBeAbleToReadFromSerialPortAndGetAnEntry() {
        SensorReadingProducer underTest = new SensorReadingProducer("owner.factory");
        underTest.findPortAndOpen();
        String bufferContents = underTest.getSingleReading();
        while (bufferContents.equals("")) {
            bufferContents = underTest.getSingleReading();
        }
        assertTrue(bufferContents.startsWith("[{time"));
        assertTrue(bufferContents.endsWith("]"));
        underTest.close();
    }
}