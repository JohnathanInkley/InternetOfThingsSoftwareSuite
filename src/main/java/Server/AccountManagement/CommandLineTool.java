package Server.AccountManagement;

import Server.DatabaseStuff.ClientDatabaseEditor;
import Server.DatabaseStuff.Database;

import java.util.List;
import java.util.Scanner;

public class CommandLineTool {
    private static String RESOURCE_FILE_PATH = "src/main/java/Server/AccountManagement/Resources";
    private static String STRING_TO_HANDLER_FILE = "choiceToHandlerLookup.config";
    private static String SELECTION_SCREEN_PREFIX = "0";

    private static final String CLIENT_DATABASE_NAME = "ClientManagementDatabase";
    private static final String DATABASE_URL = "http://localhost:8086/";
    private final ClientDatabaseEditor databaseEditor;
    private final Database database;

    private ResourceFileReader resourceReader;
    private UserChoiceHandlerGenerator handlerGenerator;
    private Scanner commandLineScanner;

    private boolean userWishesToQuit = false;
    private String currentUserChoice;

    public static void main(String[] args) {
        CommandLineTool tool = new CommandLineTool();
        tool.run();
    }

    public CommandLineTool() {
        commandLineScanner = new Scanner(System.in);
        handlerGenerator = new UserChoiceHandlerGenerator(RESOURCE_FILE_PATH + "/" + STRING_TO_HANDLER_FILE);
        database = new Database(CLIENT_DATABASE_NAME, DATABASE_URL);
        databaseEditor = new ClientDatabaseEditor(database);
    }

    private void run() {
        readInTextResources();
        while (!userWishesToQuit) {
            promptUserForChoice();
            dealWithUserChoice();
        }
    }

    private void readInTextResources() {
        try {
            resourceReader = new ResourceFileReader(RESOURCE_FILE_PATH, ".txt");
        } catch (Exception e) {
            userWishesToQuit = true;
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    public void promptUserForChoice() {
        List<String> selectionText = resourceReader.getTextLinesForResource(SELECTION_SCREEN_PREFIX);
        for (String line : selectionText) {
            System.out.println(line);
        }
        currentUserChoice = commandLineScanner.nextLine();
        System.out.println();
    }

    private void dealWithUserChoice() {
        try {
            if (currentUserChoice.equals("q")) {
                userWishesToQuit = true;
            } else {
                UserChoiceHandler userChoiceHandler = handlerGenerator.getHandler(currentUserChoice);
                userChoiceHandler.processUserChoice(resourceReader.getTextLinesForResource(currentUserChoice), this);
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

    }

    public ClientDatabaseEditor getDatabaseEditor() {
        return databaseEditor;
    }

    public Scanner getCommandLineScanner() {
        return commandLineScanner;
    }
}
