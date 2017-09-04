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
        System.out.println("1");
        DatabaseEntry entry = database.getLatestEntryForParticularLabel(clientName + "." + siteName,IP_LABEL, sensorIP);
        System.out.println("2");
        listOfColumnHeadings = new ArrayList<>();
        System.out.println("3");
        for (DatabaseEntryField field : entry) {
            addLabelIfValueNotNull(field);
        }
        System.out.println("4");
        return listOfColumnHeadings;
    }

    private void addLabelIfValueNotNull(DatabaseEntryField field) {
        String fieldName = field.getFieldName();
        if (!fieldName.equals("DeviceCollection") && !fieldName.equals(IP_LABEL) && field.getFieldValue() != null) {
            listOfColumnHeadings.add(field.getFieldName());
        }
    }

    public Map<String, List<String>> getLabelMap(List<String> listIP, String clientName, String siteName) {
        ipToLabelListMap = new HashMap<>();
        labelsForSite = new HashSet<>();
        for (String ip : listIP) {
            try {
                List<String> labelsForIP = getLabels(ip, clientName, siteName);
                labelsForSite.addAll(labelsForIP);
                ipToLabelListMap.put(ip, labelsForIP);
            } catch (Exception e) {

            }
        }
        return ipToLabelListMap;
    }

    public Set<String> getLabelsForSite(List<String> listIP, String clientName, String siteName) {
        getLabelMap(listIP, clientName, siteName);
        return labelsForSite;
    }
}
