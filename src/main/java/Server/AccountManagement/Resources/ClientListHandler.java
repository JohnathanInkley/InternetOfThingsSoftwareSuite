package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.util.List;
import java.util.Scanner;

public class ClientListHandler implements UserChoiceHandler {
    CommandLineTool commandLineTool;
    private List<String> linesOfTextForChoice;
    private ClientDatabaseEditor editor;
    private Scanner scanner;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        editor = commandLineTool.getDatabaseEditor();
        scanner = commandLineTool.getCommandLineScanner();

        printFirstLine();
        System.out.println();
        printListOfClients();
        System.out.println();
        printSitesForAGivenClient();
        System.out.println();
    }

    private void printFirstLine() {
        System.out.println(linesOfTextForChoice.get(0));
    }

    private void printListOfClients() {
        List<String> clientNames = editor.getClientNames();
        for (String client : clientNames) {
            printClientRowInTable(client);
        }
    }

    private void printClientRowInTable(String client) {
        String filledText = new String(linesOfTextForChoice.get(1));
        int numSitesForClient = editor.getSiteNamesForClient(client).size();
        filledText = filledText.replace("$CLIENT_NAME", client)
                                .replace("$SITE_NUM", Integer.toString(numSitesForClient));
        System.out.println(filledText);
    }

    private void printSitesForAGivenClient() {
        System.out.println(linesOfTextForChoice.get(2));
        String clientName = scanner.nextLine();
        try {
            getSitesForClientAndPrint(clientName);
        } catch (Exception e) {
            System.out.println("No client found named " + clientName);
        }
    }

    private void getSitesForClientAndPrint(String clientName) {
        List<String> siteNamesForClient = editor.getSiteNamesForClient(clientName);
        String text = linesOfTextForChoice.get(3).replace("$CLIENT_NAME", clientName);
        System.out.println(text);
        for (String siteName : siteNamesForClient) {
            int numSensors = editor.getSensorsForClientSite(clientName, siteName).size();
            String line = linesOfTextForChoice.get(4).replace("$SITE_NAME", siteName)
                    .replace("$SENSOR_NUM", Integer.toString(numSensors));
            System.out.println(line);
        }
    }
}
