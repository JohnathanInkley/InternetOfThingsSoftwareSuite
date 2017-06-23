package Server.AccountManagement;

import java.util.List;
import java.util.Scanner;

public class CommandLineTool {
    private static String RESOURCE_FILE_PATH = "src/main/java/Server/AccountManagement/Resources";
    private static String STRING_TO_HANDLER_FILE = "choiceToHandlerLookup.config";
    private static String SELECTION_SCREEN_PREFIX = "0";

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
    }

    private void run() {
        readInTextResources();
        while (!userWishesToQuit) {
            promptUserForChoice();
            dealWithUserChoice();
        }
    }

    private void readInTextResources() {
        resourceReader = new ResourceFileReader(RESOURCE_FILE_PATH, ".txt");
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
        if (currentUserChoice.equals("q")) {
            userWishesToQuit = true;
        } else {
            UserChoiceHandler userChoiceHandler = handlerGenerator.getHandler(currentUserChoice);
            userChoiceHandler.processUserChoice();
        }
    }

}
