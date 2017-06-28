package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.PhysicalLocationStuff.SensorLocation;

import java.util.List;
import java.util.Scanner;

public class NewSensorLocationsForSiteHandler implements UserChoiceHandler {

    private List<String> linesOfTextForChoice;
    private CommandLineTool commandLineTool;
    private Scanner scanner;
    private ClientDatabaseEditor editor;
    private String clientName;
    private String siteName;
    private Integer numSensors;
    private int sensorCount;
    private String IP;
    private Double lat;
    private Double lon;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        scanner = commandLineTool.getCommandLineScanner();
        editor = commandLineTool.getDatabaseEditor();

        getClientName();
    }

    private void getClientName() {
        System.out.println(linesOfTextForChoice.get(0));
        clientName = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(1).replace("$CLIENT_NAME", clientName));
        if ("y".equals(scanner.nextLine())) {
            getSiteName();
        }
    }

    private void getSiteName() {
        System.out.println(linesOfTextForChoice.get(2));
        siteName = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(3).replace("$SITE_NAME", siteName));
        if ("y".equals(scanner.nextLine())) {
            getNumSensors();
        }
    }

    private void getNumSensors() {
        System.out.println(linesOfTextForChoice.get(4));
        numSensors = Integer.valueOf(scanner.nextLine());
        System.out.println(linesOfTextForChoice.get(5).replace("$NUM_SENSORS", Integer.toString(numSensors)));
        if ("y".equals(scanner.nextLine())) {
            addSensors();
        } else {
            getNumSensors();
        }
    }

    private void addSensors() {
        sensorCount = 1;
        while (sensorCount <= numSensors) {
            addIndividualSensor();
            sensorCount++;
        }
        System.out.println(linesOfTextForChoice.get(11)
                            .replace("$NUM_SENSORS", numSensors.toString())
                            .replace("$CLIENT_NAME", clientName)
                            .replace("$SITE_NAME", siteName));
    }

    private void addIndividualSensor() {
        System.out.println(linesOfTextForChoice.get(6).replace("$SENSOR_COUNT", Integer.toString(sensorCount)));
        IP = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(7).replace("$SENSOR_COUNT", Integer.toString(sensorCount)));
        lat = Double.valueOf(scanner.nextLine());
        System.out.println(linesOfTextForChoice.get(8).replace("$SENSOR_COUNT", Integer.toString(sensorCount)));
        lon = Double.valueOf(scanner.nextLine());
        if (checkSensorDetailsCorrect()) {
            addSensorToDatabase();
        } else {
            addIndividualSensor();
        }
    }

    private boolean checkSensorDetailsCorrect() {
        System.out.println(linesOfTextForChoice.get(9)
                        .replace("$IP", IP)
                        .replace("$LAT", lat.toString())
                        .replace("$LON", lon.toString()));
        return "y".equals(scanner.nextLine());
    }


    private void addSensorToDatabase() {
        SensorLocation sensor = new SensorLocation(IP, lat, lon);
        try {
            editor.addSensorToClientSite(clientName, siteName, sensor);
        } catch (Exception e) {
            System.out.println("Sensor could not be added to site, please check details entered for site and client");
        }
    }
}
