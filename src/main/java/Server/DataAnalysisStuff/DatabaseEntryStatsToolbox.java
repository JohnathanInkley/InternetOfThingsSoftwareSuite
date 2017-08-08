package Server.DataAnalysisStuff;

import Server.DatabaseStuff.DatabaseEntry;
import Server.DatabaseStuff.DatabaseEntrySet;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;

import static Server.DatabaseStuff.DatabaseEntry.TIMESTAMP_FORMAT;

public class DatabaseEntryStatsToolbox {

    public double getMean(DatabaseEntrySet entrySet, String fieldName) {
        ArrayList<DatabaseEntry> entryArrayList = generateArrayListOfEntries(entrySet);
        SummaryStatistics statsCalculator = addEntriesToStatsCalculator(entryArrayList, fieldName);
        return statsCalculator.getMean();
    }

    private ArrayList<DatabaseEntry> generateArrayListOfEntries(DatabaseEntrySet entrySet) {
        ArrayList<DatabaseEntry> entriesAsArray = new ArrayList<>();
        for (int i = 0; i < entrySet.size(); i++) {
            entriesAsArray.add(entrySet.get(i));
        }
        return entriesAsArray;
    }

    private SummaryStatistics addEntriesToStatsCalculator(ArrayList<DatabaseEntry> entryArray, String fieldName) {
        SummaryStatistics statsCalculator = new SummaryStatistics();
        for (int i = 0; i < entryArray.size(); i++) {
            statsCalculator.addValue((Double) entryArray.get(i).get(fieldName));
        }
        return statsCalculator;
    }

    public double getStandardDeviation(DatabaseEntrySet entrySet, String fieldName) {
        ArrayList<DatabaseEntry> entryArrayList = generateArrayListOfEntries(entrySet);
        SummaryStatistics statsCalculator = addEntriesToStatsCalculator(entryArrayList, fieldName);
        return statsCalculator.getStandardDeviation();
    }

    public ArrayList<AbstractMap.SimpleEntry<String, Double>> getMeanForIntervals(DatabaseEntrySet entrySet,
                                                                                  String fieldName,
                                                                                  int intervalLengthInMS) {
        ArrayList<DatabaseEntry> entriesAsArray = generateArrayListOfEntries(entrySet);
        DatabaseEntry minEntry = findMinTimestampEntry(entriesAsArray);
        ArrayList<ArrayList<DatabaseEntry>> partitionedData = partitionDataByIntervalLength(intervalLengthInMS, entriesAsArray, minEntry);
        return generateMeansFromPartitionedData(fieldName, intervalLengthInMS, minEntry, partitionedData);
    }

    private DatabaseEntry findMinTimestampEntry(ArrayList<DatabaseEntry> entriesAsArray) {
        DatabaseEntry minEntry = entriesAsArray.get(0);
        for (DatabaseEntry entry : entriesAsArray) {
            if (entry.getLongTimeInMilliseconds() < minEntry.getLongTimeInMilliseconds()) {
                minEntry = entry;
            }
        }
        return minEntry;
    }

    private ArrayList<ArrayList<DatabaseEntry>> partitionDataByIntervalLength(int intervalLengthInMS, ArrayList<DatabaseEntry> entriesAsArray, DatabaseEntry minTimeEntry) {
        long minTimestamp = minTimeEntry.getLongTimeInMilliseconds();
        ArrayListPartitioner<DatabaseEntry> partitioner = new ArrayListPartitioner<>(entriesAsArray);
        return partitioner.partition(
                (entry) -> Math.toIntExact((entry.getLongTimeInMilliseconds() - minTimestamp) / intervalLengthInMS)
        );
    }

    private ArrayList<AbstractMap.SimpleEntry<String, Double>> generateMeansFromPartitionedData(String fieldName, int intervalLengthInMS, DatabaseEntry minEntry, ArrayList<ArrayList<DatabaseEntry>> partitionedData) {
        ArrayList<AbstractMap.SimpleEntry<String, Double>> meanForEachInterval = new ArrayList<>();
        long timeAsLong = minEntry.getLongTimeInMilliseconds();
        for (ArrayList<DatabaseEntry> dataInInterval : partitionedData) {
            SummaryStatistics statsForInterval = addEntriesToStatsCalculator(dataInInterval, fieldName);
            Double meanForInterval = statsForInterval.getMean();
            TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            String timestampForInterval = TIMESTAMP_FORMAT.format(new Date(timeAsLong));
            meanForEachInterval.add(new AbstractMap.SimpleEntry<>(timestampForInterval, meanForInterval));
            timeAsLong += intervalLengthInMS;
        }
        return meanForEachInterval;
    }

    public ArrayList<AbstractMap.SimpleEntry<String, Double>> getSdForIntervals(DatabaseEntrySet entrySet,
                                                                                String fieldName,
                                                                                int intervalLengthInMS) {
        ArrayList<DatabaseEntry> entriesAsArray = generateArrayListOfEntries(entrySet);
        DatabaseEntry minEntry = findMinTimestampEntry(entriesAsArray);
        ArrayList<ArrayList<DatabaseEntry>> partitionedData = partitionDataByIntervalLength(intervalLengthInMS, entriesAsArray, minEntry);
        return generateSdFromPartitionedData(fieldName, intervalLengthInMS, minEntry, partitionedData);
    }

    private ArrayList<AbstractMap.SimpleEntry<String, Double>> generateSdFromPartitionedData(String fieldName, int intervalLengthInMS, DatabaseEntry minEntry, ArrayList<ArrayList<DatabaseEntry>> partitionedData) {
        ArrayList<AbstractMap.SimpleEntry<String, Double>> sdForEachInterval = new ArrayList<>();
        long timeAsLong = minEntry.getLongTimeInMilliseconds();
        for (ArrayList<DatabaseEntry> dataInInterval : partitionedData) {
            SummaryStatistics statsForInterval = addEntriesToStatsCalculator(dataInInterval, fieldName);
            Double sdForInterval = statsForInterval.getStandardDeviation();
            TIMESTAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            String timestampForInterval = TIMESTAMP_FORMAT.format(new Date(timeAsLong));
            sdForEachInterval.add(new AbstractMap.SimpleEntry<>(timestampForInterval, sdForInterval));
            timeAsLong += intervalLengthInMS;
        }
        return sdForEachInterval;
    }
}

