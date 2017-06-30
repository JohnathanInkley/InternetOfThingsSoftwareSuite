package Server.AccountManagement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UserConfigFileGenerator {
    ArrayList<UsernamePasswordPair> usernamePasswordPairs;

    public void initialiseNewConfigFile() {
        usernamePasswordPairs = new ArrayList<>();
    }

    public void generateOutputFile(String outputFilePath) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFilePath));
            for (UsernamePasswordPair pair : usernamePasswordPairs) {
                writer.write("username: " + pair.username + " password: " + pair.password);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Output file could not be written for path " + outputFilePath);
        }
    }

    public void addUser(UsernamePasswordPair usernamePasswordPair) {
        usernamePasswordPairs.add(usernamePasswordPair);
    }
}
