package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.util.List;
import java.util.Scanner;

public class NewClientHandler implements UserChoiceHandler {
    private List<String> linesOfTextForChoice;
    private CommandLineTool commandLineTool;
    private ClientDatabaseEditor editor;
    private Scanner scanner;
    private String clientName;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        editor = commandLineTool.getDatabaseEditor();
        scanner = commandLineTool.getCommandLineScanner();

        printFirstLines();
        getNameOfNewClientFromUser();
        checkIfUserChoiceCorrect();
    }

    private void printFirstLines() {
        System.out.println(linesOfTextForChoice.get(0));
        System.out.println(linesOfTextForChoice.get(1));
    }

    private void getNameOfNewClientFromUser() {
        clientName = scanner.nextLine();
    }


    private void checkIfUserChoiceCorrect() {
        String text = linesOfTextForChoice.get(2);
        text = text.replace("$CLIENT_NAME", clientName);
        System.out.println(text);
        if ("y".equals(scanner.nextLine())) {
            editor.createNewClient(clientName);
        }
    }
}
