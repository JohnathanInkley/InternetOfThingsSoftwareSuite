package Server.PhysicalLocationStuff;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class sensorLocationTest {

    @Test
    public void shouldBeAbleToGetLatitudeAndLongitudeFromLocation() {
        SensorLocation location = new SensorLocation("Here", 1, 2);
        assertEquals(1.0, location.getLatitude(),0.001);
        assertEquals(2.0, location.getLongitude(),0.001);
    }

    @Test
    public void shouldBeAbleToCalculateDistanceBetweenLocations() {
        SensorLocation first = new SensorLocation("first", 1, 1);
        SensorLocation second = new SensorLocation("second", 1, 2);
        assertEquals(69.132, first.distanceTo(second),0.1);
    }

    @Test
    public void sameLocationsShouldBeEqual() {
        SensorLocation first = new SensorLocation("first", 1, 1);
        SensorLocation equalLocation = new SensorLocation("second", 1, 1);
        SensorLocation notEqualLocation = new SensorLocation("third", 0, 0);
        assertTrue(first.equals(equalLocation));
        assertFalse(first.equals(notEqualLocation));
    }

}