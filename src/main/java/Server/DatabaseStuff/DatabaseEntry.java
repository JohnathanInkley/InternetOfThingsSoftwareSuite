package Server.DatabaseStuff;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatabaseEntry implements Iterable<DatabaseEntryField>, Serializable {
    public static SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private HashMap<String, Object> fields;
    private String timestamp;

    public DatabaseEntry() {
        fields = new HashMap<>();
    }

    public void setDeviceCollectionIdentifier(DeviceCollection site) {
        fields.put("DeviceCollection", site.identifier());
    }

    public String getDeviceCollectionIdentifier() {
        return (String) fields.get("DeviceCollection");
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestampFormat(SimpleDateFormat timestampFormat) {
        DatabaseEntry.timestampFormat = timestampFormat;
    }

    public void add(String fieldName, Object  fieldValue) {
        fields.put(fieldName, fieldValue);
    }

    public Object get(String field) {
        return fields.get(field);
    }

    public void remove(String field) {
        fields.remove(field);
    }

    public int getNumberOfFields() {
        return fields.size();
    }

    @Override
    public Iterator<DatabaseEntryField> iterator(){
        return new Iterator<DatabaseEntryField> () {
            private final Iterator<Map.Entry<String, Object>> iterator = fields.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public DatabaseEntryField next() {
                Map.Entry<String, Object> entry = iterator.next();
                return new DatabaseEntryField(entry.getKey(), entry.getValue());
            }
        };
    }

    @Override
    public boolean equals(Object other) {
        DatabaseEntry otherEntry = (DatabaseEntry) other;
        if (timestamp == null && otherEntry.timestamp == null) {
            return fields.equals(otherEntry.fields);
        } else if (timestamp == null | otherEntry == null | timestampFormat == null | otherEntry.timestampFormat == null) {
            return false;
        } else {
            return fields.equals(otherEntry.fields) && getLongTimeInMilliseconds() == otherEntry.getLongTimeInMilliseconds();
        }

    }

    public int numberOfFields() {
        return fields.size();
    }

    @Override
    public String toString() {
        return "Time: " + timestamp + " Fields: " + fields.toString();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public long getLongTimeInMilliseconds() {
        try {
            return timestampFormat.parse(timestamp).getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private long produceIdAsLong() throws ParseException {
        try {
            return Long.valueOf(timestamp);
        } catch (NumberFormatException e) { // When ID entries come out of database, they have timestamp as ID + date(1970/01/01) so need to revert to ID
            return timestampFormat.parse(timestamp + ".000").getTime()
                    - timestampFormat.parse("1970-01-01 00:00:00.000").getTime();
        }
    }
}
