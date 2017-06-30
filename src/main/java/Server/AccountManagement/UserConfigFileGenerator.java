package Server.AccountManagement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserConfigFileGenerator {
    ArrayList<UserNamePasswordPair> userNamePasswordPairs;

    public void initialiseNewConfigFile() {
        userNamePasswordPairs = new ArrayList<>();
    }

    public void generateOutputFile(String outputFilePath) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            for (UserNamePasswordPair pair : userNamePasswordPairs) {
                writer.write("username: " + pair.userName + " password: " + pair.password);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Output file could not be written for path " + outputFilePath);
        }
    }

    public void addUser(UserNamePasswordPair userNamePasswordPair) {
        userNamePasswordPairs.add(userNamePasswordPair);
    }
}
