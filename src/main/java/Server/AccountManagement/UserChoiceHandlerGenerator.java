package Server.AccountManagement;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

public class UserChoiceHandlerGenerator {

    private final String configFile;
    private HashMap<String, String> choiceToClassNameMap;

    public UserChoiceHandlerGenerator(String configFile) {
        choiceToClassNameMap = new HashMap<>();
        this.configFile = configFile;
        readInClassNamesFromConfigFile();
    }

    private void readInClassNamesFromConfigFile() {
        try {
            List<String> fileContents = Files.readLines(new File(configFile), Charset.defaultCharset());
            for (String choiceToHandlerEntry : fileContents) {
                String[] splitEntry = choiceToHandlerEntry.split("-");
                choiceToClassNameMap.put(splitEntry[0], splitEntry[1]);
            }
        } catch (Exception e) {
            throw new RuntimeException("Config file is either missing or badly formed, please check and try again");
        }
    }

    public UserChoiceHandler getHandler(String userChoice) {
        String className = getClassNameFromChoice(userChoice);
        try {
            Class<?> handlerClass = Class.forName(className);
            return (UserChoiceHandler) handlerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find handler for " + userChoice + " (user choice)");
        }
    }

    private String getClassNameFromChoice(String userChoice) {
        String className = choiceToClassNameMap.get(userChoice);
        if (className != null) {
            return className;
        } else {
            throw new RuntimeException("Choice " + userChoice + " not specified in config file");
        }
    }
}
