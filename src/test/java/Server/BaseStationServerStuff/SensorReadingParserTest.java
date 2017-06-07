package Server.BaseStationServerStuff;

import Server.DatabaseStuff.DatabaseEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SensorReadingParserTest {

    @Test
    public void shouldGetTimeForReading() {
        SensorReadingParser underTest = new SensorReadingParser();
        String reading = "[{time,,2000-01-01 12:34:56.123}]";
        DatabaseEntry parsedReading = underTest.parseReading(reading);
        assertEquals("2000-01-01 12:34:56.123", parsedReading.getTimestamp());
    }

    @Test
    public void shouldGetTimestampAndSingleField() {
        SensorReadingParser underTest = new SensorReadingParser();
        String reading = "[{time,,2000-01-01 12:34:56.123};{OPT,lux,8.26}]";
        DatabaseEntry parsedReading = underTest.parseReading(reading);
        assertEquals("2000-01-01 12:34:56.123", parsedReading.getTimestamp());
        assertEquals(8.26, parsedReading.get("OPT.lux"));
    }

    @Test
    public void shouldGetTimestampAndDoubleAndStringFields() {
        SensorReadingParser underTest = new SensorReadingParser();
        String reading = "[{time,,2000-01-01 12:34:56.123};{OPT,lux,8.26};{IP,,ab20:zf4}]";
        DatabaseEntry parsedReading = underTest.parseReading(reading);
        assertEquals("2000-01-01 12:34:56.123", parsedReading.getTimestamp());
        assertEquals(8.26, parsedReading.get("OPT.lux"));
        assertEquals("ab20:zf4", parsedReading.get("IP"));
    }

}