package Server.DatabaseStuff;

import java.util.*;

public class SensorLabelMapGenerator {

    public static final String IP_LABEL = "IP";

    private Database database;
    private ArrayList<String> listOfColumnHeadings;
    private HashMap ipToLabelListMap;
    private HashSet<String> labelsForSite;

    public SensorLabelMapGenerator(Database database) {
        this.database = database;
    }

    public List<String> getLabels(String sensorIP, String clientName, String siteName) {
        System.out.println("getting labels: " + 1);
        DatabaseEntry entry = database.getLatestEntryForParticularLabel(clientName + "." + siteName,IP_LABEL, sensorIP);
        System.out.println("getting labels:yo " + entry);
        listOfColumnHeadings = new ArrayList<>();
        for (DatabaseEntryField field : entry) {
            addLabelIfValueNotNull(field);
        }
        return listOfColumnHeadings;
    }

    private void addLabelIfValueNotNull(DatabaseEntryField field) {
        String fieldName = field.getFieldName();
        if (!fieldName.equals("DeviceCollection") && !fieldName.equals(IP_LABEL) && field.getFieldValue() != null) {
            listOfColumnHeadings.add(field.getFieldName());
        }
    }

    public Map<String, List<String>> getLabelMap(List<String> listIP, String clientName, String siteName) {
        System.out.println("start here1");
        ipToLabelListMap = new HashMap<>();
        labelsForSite = new HashSet<>();
        System.out.println("start here2");
        for (String ip : listIP) {
            System.out.println("IP: " + ip);
            List<String> labelsForIP = getLabels(ip, clientName, siteName);
            System.out.println("labels: " + labelsForIP);
            labelsForSite.addAll(labelsForIP);
            ipToLabelListMap.put(ip, labelsForIP);
        }
        return ipToLabelListMap;
    }

    public Set<String> getLabelsForSite(List<String> listIP, String clientName, String siteName) {
        getLabelMap(listIP, clientName, siteName);
        return labelsForSite;
    }
}
