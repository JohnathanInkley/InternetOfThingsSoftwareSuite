package Server.AccountManagement.Resources;

import Server.AccountManagement.CommandLineTool;
import Server.AccountManagement.UserChoiceHandler;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.util.List;
import java.util.Scanner;

public class NewSitesForClientHandler implements UserChoiceHandler {
    private List<String> linesOfTextForChoice;
    private CommandLineTool commandLineTool;
    private ClientDatabaseEditor editor;
    private Scanner scanner;
    private String clientName;
    private String siteName;

    @Override
    public void processUserChoice(List<String> linesOfTextForChoice, CommandLineTool commandLineTool) {
        this.linesOfTextForChoice = linesOfTextForChoice;
        this.commandLineTool = commandLineTool;
        editor = commandLineTool.getDatabaseEditor();
        scanner = commandLineTool.getCommandLineScanner();

        printFirstLines();
        getClientUserWishesToAddSiteFor();
        System.out.println();

    }

    private void printFirstLines() {
        System.out.println(linesOfTextForChoice.get(0));
    }

    private void getClientUserWishesToAddSiteFor() {
        clientName = scanner.nextLine();
        String text = linesOfTextForChoice.get(2);
        text = text.replace("$CLIENT_NAME", clientName);
        System.out.println(text);
        if ("y".equals(scanner.nextLine())) {
            getSiteFromUser();
        }

    }

    private void getSiteFromUser() {
        System.out.println(linesOfTextForChoice.get(3));
        siteName = scanner.nextLine();
        String text = linesOfTextForChoice.get(5);
        text = text.replace("$SITE_NAME", siteName);
        System.out.println(text);
        if ("y".equals(scanner.nextLine())) {
            attemptToAddSiteForClient();
        }
    }

    private void attemptToAddSiteForClient() {
        try {
            editor.addSiteForClient(clientName, siteName);
            System.out.println(linesOfTextForChoice.get(6).replace("$SITE_NAME", siteName));
        } catch (Exception e) {
            System.out.println("No client found named " + clientName);
        }
    }
}
