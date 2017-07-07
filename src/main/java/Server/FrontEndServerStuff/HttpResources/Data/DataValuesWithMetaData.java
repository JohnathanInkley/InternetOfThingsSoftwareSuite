package Server.FrontEndServerStuff.HttpResources.Data;

public class DataValuesWithMetaData {

    public String timestamp;
    public String sensorIP;
    public String label;
    public Double value;

    public DataValuesWithMetaData(String timestamp, String sensorIP, String label, Double value) {
        this.timestamp = timestamp;
        this.sensorIP = sensorIP;
        this.label = label;
        this.value = value;
    }

    public String toString() {
        return "{timestamp : " + timestamp + ", sensorIP: " + sensorIP + ", label: " + label + ", value: " + value + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataValuesWithMetaData that = (DataValuesWithMetaData) o;

        if (!timestamp.equals(that.timestamp)) return false;
        if (!sensorIP.equals(that.sensorIP)) return false;
        if (!label.equals(that.label)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = timestamp.hashCode();
        result = 31 * result + sensorIP.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
