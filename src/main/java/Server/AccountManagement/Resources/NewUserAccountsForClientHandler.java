package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.util.List;
import java.util.Scanner;

public class NewUserAccountsForClientHandler implements UserChoiceHandler {
    private List<String> linesOfTextForChoice;
    private CommandLineTool commandLineTool;
    private ClientDatabaseEditor editor;
    private Scanner scanner;
    private String clientName;
    private Integer numAccounts;
    private String fileName;
    private String areAdminAccounts;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        editor = commandLineTool.getDatabaseEditor();
        scanner = commandLineTool.getCommandLineScanner();

        printFirstLines();
        System.out.println();
    }

    private void printFirstLines() {
        System.out.println(linesOfTextForChoice.get(0));
        System.out.println(linesOfTextForChoice.get(1));

        getClientName();
    }

    private void getClientName() {
        clientName = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(2).replace("$CLIENT_NAME", clientName));
        if ("y".equals(scanner.nextLine())) {
            getAreTheyAdminAccounts();
        } else {
            getClientName();
        }
    }

    private void getAreTheyAdminAccounts() {
        System.out.println(linesOfTextForChoice.get(3));
        areAdminAccounts = scanner.nextLine();
        getNumberOfAccountsNeeded();
    }

    private void getNumberOfAccountsNeeded() {
        System.out.println(linesOfTextForChoice.get(4));
        numAccounts = Integer.valueOf(scanner.nextLine());
        System.out.println(linesOfTextForChoice.get(5).replace("$NUM_ACCOUNTS", numAccounts.toString()));
        if ("y".equals(scanner.nextLine())) {
            getFileNameForUserConfigFile();
        } else {
            getAreTheyAdminAccounts();
        }
    }

    private void getFileNameForUserConfigFile() {
        System.out.println(linesOfTextForChoice.get(6));
        fileName = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(7).replace("$FILE_NAME", fileName));
        if ("y".equals(scanner.nextLine())) {
            generateNewAccounts();
        } else {
            getFileNameForUserConfigFile();
        }
    }

    private void generateNewAccounts() {
        if ("y".equals(areAdminAccounts)) {
            editor.generateNewAdminsForClient(clientName, numAccounts, fileName);
        } else {
            editor.generateNewUsersForClient(clientName, numAccounts, fileName);
        }
        System.out.println(linesOfTextForChoice.get(8).replace("$FILE_NAME", fileName)
                                                        .replace("$NUM_ACCOUNTS", numAccounts.toString())
                                                        .replace("CLIENT_NAME", clientName));
    }
}
