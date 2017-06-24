package Server.DatabaseStuff;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Database {
    private String name;
    private InfluxDB database;

    public Database(String name, String url) {
        this.name = name;
        try {
            database = InfluxDBFactory.connect(url);
            database.createDatabase(name);
        } catch (Exception e) {
            throw new RuntimeException("Could not connect to given url");
        }
    }

    public boolean addEntry(DatabaseEntry entry) {
        Map<String, Object> fieldMap = generateFieldMap(entry);
        Point point = Point
                .measurement(entry.getDeviceCollectionIdentifier())
                .fields(fieldMap)
                .time(entry.getLongTimeInMilliseconds(), TimeUnit.MILLISECONDS)
                .build();
        database.write(name, "autogen", point);
        return true;
    }

    private Map<String, Object> generateFieldMap(DatabaseEntry sampleEntry) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (DatabaseEntryField field : sampleEntry) {
            fieldMap.put(field.getFieldName(), field.getFieldValue());
        }
        return fieldMap;
    }

    public void removeAllWithCertainValue(String tableName, String field, String fieldValue) {
        database.query(new Query("DELETE FROM \"" + tableName + "\" " +
                "WHERE \"" + field + "\" = \'" + fieldValue + "\' ", name));
    }

    public DatabaseEntrySet getEntries(String tableName, String field, String fieldValue) {
        Query query = new Query("SELECT * FROM \"" + tableName + "\" " +
                "WHERE \"" + field + "\" = \'" + fieldValue + "\' ", name);
        return getResultsSetFromQuery(query);
    }



    public DatabaseEntrySet getSiteEntriesBetween(DeviceCollection site, long beforeTimeInMS, long afterTimeInMS) {
        Query query = new Query("SELECT * FROM \"" + site.identifier() + "\" " +
                                "WHERE time > " + beforeTimeInMS + "ms " +
                                "AND time < " + afterTimeInMS + "ms " +
                                "", name);

        return getResultsSetFromQuery(query);
    }

    public DatabaseEntrySet getSiteEntriesBetween(DeviceCollection site, String beforeDate, String afterDate) {
        Query query = new Query("SELECT * FROM \"" + site.identifier() + "\" " +
                "WHERE time > '" + beforeDate + "' " +
                "AND time < '" + afterDate + "' " +
                "", name);

        return getResultsSetFromQuery(query);
    }

    public double getMeanSiteEntriesForFieldBetween(DeviceCollection site, String fieldName, String beforeDate, String afterDate) {
        Query query = new Query("SELECT MEAN(" + fieldName + ") " +
                "FROM \"" + site.identifier() + "\" " +
                "WHERE time > '" + beforeDate + "' " +
                "AND time < '" + afterDate + "'" +
                "",name);
        DatabaseEntrySet results = getResultsSetFromQuery(query);
        if (results.size() != 0) {
            DatabaseEntry meanEntry = results.get(0);
            return (Double) meanEntry.get("mean");
        } else {
            throw new RuntimeException("No entries found so mean not defined");
        }
    }

    private DatabaseEntrySet getResultsSetFromQuery(Query query) {
        QueryResult queryResults = database.query(query);
        List<QueryResult.Result> queryList = queryResults.getResults();
        DatabaseEntrySet entrySet = new DatabaseEntrySet();
        for (QueryResult.Result result : queryList) {
            processIndividualQueryResult(result, entrySet);
        }
        return entrySet;
    }

    private void processIndividualQueryResult(QueryResult.Result individualResult, DatabaseEntrySet entrySetToAddResultTo) {
        if (individualResult.getSeries() == null) {
            return;
        }

        for (QueryResult.Series individualSeries : individualResult.getSeries()) {
            List<String> columnLabels = individualSeries.getColumns();
            for (List<Object> individualEntry : individualSeries.getValues()) {
                DatabaseEntry entry = produceDatabaseEntryObjectFromActualEntry(columnLabels, individualEntry);
                entrySetToAddResultTo.add(entry);
            }
        }
    }

    private DatabaseEntry produceDatabaseEntryObjectFromActualEntry(List<String> columnLabels, List<Object> individualEntry) {
        DatabaseEntry entry = new DatabaseEntry();
        for (int i = 0; i < columnLabels.size(); i++) {
            entry.add(columnLabels.get(i), individualEntry.get(i));
        }
        String time = ((String) entry.get("time")).replace("Z", "").replace("T", " ");
        entry.setTimestamp(time);
        entry.remove("time");
        return entry;
    }

    public void deleteDatabase(String databaseName) {
        database.deleteDatabase(databaseName);
    }

}
