package BaseStation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SensorReadingHandler implements Serializable {
    private Set<String> setOfSensorReadings;

    public SensorReadingHandler() {
        setOfSensorReadings = new HashSet<>();
    }

    public void add(String singleReading) {
        setOfSensorReadings.add(singleReading);
    }

    public String get() {
        String reading = setOfSensorReadings.iterator().next();
        setOfSensorReadings.remove(reading);
        return reading;
    }

    public boolean isEmpty() {
        return setOfSensorReadings.isEmpty();
    }

    public int size() {
        return setOfSensorReadings.size();
    }
}
