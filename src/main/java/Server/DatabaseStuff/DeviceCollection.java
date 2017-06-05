package Server.DatabaseStuff;

public class DeviceCollection {
    private String siteName;
    private String ownerName;

    public DeviceCollection(String ownerName, String siteName) {
        this.siteName = siteName;
        this.ownerName = ownerName;
    }

    public String name() {
        return siteName;
    }

    public String ownerName() {
        return ownerName;
    }

    public String identifier() {
        return ownerName + "." + siteName;
    }

    @Override
    public boolean equals(Object other) {
        DeviceCollection otherCollection = (DeviceCollection) other;
        return identifier().equals(otherCollection.identifier());
    }

    @Override
    public int hashCode() {
        return identifier().hashCode();
    }
}
