package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.util.List;
import java.util.Scanner;

public class DeleteClientHandler implements UserChoiceHandler {
    private List<String> linesOfTextForChoice;
    private CommandLineTool commandLineTool;
    private Scanner scanner;
    private ClientDatabaseEditor editor;
    private String clientName;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        scanner = commandLineTool.getCommandLineScanner();
        editor = commandLineTool.getDatabaseEditor();

        printFirstLines();
    }

    private void printFirstLines() {
        System.out.println(linesOfTextForChoice.get(0));
        clientName = scanner.nextLine();
        System.out.println(linesOfTextForChoice.get(1).replace("$CLIENT_NAME", clientName));
        if ("y".equals(scanner.nextLine())) {
            doubleCheckTheyWantToDelete();
        } else {
            printFirstLines();
        }
        System.out.println();
    }

    private void doubleCheckTheyWantToDelete() {
        System.out.println(linesOfTextForChoice.get(2).replace("$CLIENT_NAME", clientName));
        if ("y".equals(scanner.nextLine())) {
            editor.deleteClient(clientName);
        }
    }
}
