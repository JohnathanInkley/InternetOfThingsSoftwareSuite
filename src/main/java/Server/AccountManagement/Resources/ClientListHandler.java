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
        printSitesAndUsersForAGivenClient();
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
        if (!client.equals("")) {
            int numSitesForClient = editor.getSiteNamesForClient(client).size();
            int numUsersForClient = editor.getUserNamesForClient(client).size();
            System.out.println(linesOfTextForChoice.get(1).replace("$CLIENT_NAME", client)
                    .replace("$SITE_NUM", Integer.toString(numSitesForClient))
                    .replace("$NUM_ACCOUNTS", Integer.toString(numUsersForClient)));
        }
    }

    private void printSitesAndUsersForAGivenClient() {
        System.out.println(linesOfTextForChoice.get(2));
        String clientName = scanner.nextLine();
        try {
            getSitesForClientAndPrint(clientName);
            System.out.println();
            getUsersForClientAndPrint(clientName);

        } catch (Exception e) {
            System.out.println("No client found named " + clientName);
        }
    }

    private void getSitesForClientAndPrint(String clientName) {
        List<String> siteNamesForClient = editor.getSiteNamesForClient(clientName);
        System.out.println(linesOfTextForChoice.get(3).replace("$CLIENT_NAME", clientName));
        for (String siteName : siteNamesForClient) {
            Integer numSensors = editor.getSensorsForClientSite(clientName, siteName).size();
            System.out.println(linesOfTextForChoice.get(4).replace("$SITE_NAME", siteName)
                                                          .replace("$SENSOR_NUM", numSensors.toString()));
        }
    }

    private void getUsersForClientAndPrint(String clientName) {
        List<String> userNamesForClient = editor.getUserNamesForClient(clientName);
        System.out.println(linesOfTextForChoice.get(5).replace("$CLIENT_NAME", clientName));
        for (String userName : userNamesForClient) {
            System.out.println(linesOfTextForChoice.get(6).replace("$USER_NAME", userName));
        }
    }
}
