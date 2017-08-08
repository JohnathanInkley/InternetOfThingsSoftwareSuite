package Server.DataAnalysisStuff;

import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DatabaseEntrySet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DatabaseEntryStatsToolboxTest {

    private static DatabaseEntryStatsToolbox underTest;
    private static DatabaseEntrySet entrySet;

    @BeforeClass
    public static void generateData() {
        entrySet = new DatabaseEntrySet();
        for (int i = 0; i < 4; i++) {
            DatabaseEntry entry = new DatabaseEntry();
            entry.add("temp", i + 0.0);
            entry.setTimestamp("2000-01-01 00:00:0" + i + ".000");
            entrySet.add(entry);
        }

        underTest = new DatabaseEntryStatsToolbox();
    }

    @Test
    public void shouldCalculateMeanForGivenField() {
        assertEquals(1.5, underTest.getMean(entrySet, "temp"), 0.000000001);
    }

    @Test
    public void shouldCalculateStandardDeviationForGivenField() {
        assertEquals(1.290994448735806, underTest.getStandardDeviation(entrySet, "temp"), 0.000000001);
    }

    @Test
    public void shouldCalculateMeanInFixedTimeIntervalsForGivenField() {
        ArrayList<AbstractMap.SimpleEntry<String, Double>> expectedMeans = new ArrayList<>();
        expectedMeans.add(new AbstractMap.SimpleEntry<>("2000-01-01 00:00:00.000", 0.5));
        expectedMeans.add(new AbstractMap.SimpleEntry<>("2000-01-01 00:00:02.000", 2.5));

        assertTrue(expectedMeans.containsAll(underTest.getMeanForIntervals(entrySet, "temp", 2000)));
    }

    @Test
    public void shouldCalculateSdInFixedTimeIntervalsForGivenField() {
        ArrayList<AbstractMap.SimpleEntry<String, Double>> expectedSds = new ArrayList<>();
        expectedSds.add(new AbstractMap.SimpleEntry<>("2000-01-01 00:00:00.000", 0.707106781186548));
        expectedSds.add(new AbstractMap.SimpleEntry<>("2000-01-01 00:00:02.000", 0.707106781186548));

        ArrayList<AbstractMap.SimpleEntry<String, Double>> sdForIntervals = underTest.getSdForIntervals(entrySet, "temp", 2000);
        assertEquals(expectedSds.get(0).getKey(), sdForIntervals.get(0).getKey());
        assertEquals(expectedSds.get(1).getKey(), sdForIntervals.get(1).getKey());
        assertEquals(expectedSds.get(0).getValue(), sdForIntervals.get(0).getValue(),0.0000001);
        assertEquals(expectedSds.get(1).getValue(), sdForIntervals.get(1).getValue(),0.0000001);
    }
}