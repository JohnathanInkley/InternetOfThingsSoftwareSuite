package Server.AccountManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SiteConfigFileGenerator {
    private final List<String> templateFileLines;
    private List<String> outputFileLines;
    private String aesString;

    public SiteConfigFileGenerator(String templateFilePath) {
        try {
            templateFileLines = Files.readAllLines(Paths.get(templateFilePath));
        } catch (Exception e) {
            throw new RuntimeException("Could not open template config file: " + templateFilePath);
        }
    }

    public void initialiseNewConfig() {
        outputFileLines = new ArrayList<>(templateFileLines);
        aesString = generateSecureStringForUseAsAESKey();
        addStringToLineStartingWith(aesString, "AES-Key");
    }

    private void addStringToLineStartingWith(String stringToAppend, String startingWith) {
        for (int i = 0; i < outputFileLines.size(); i++) {
            String line = outputFileLines.get(i);
            if (line.contains(startingWith)) {
                outputFileLines.set(i, line +  " " + stringToAppend);
            }
        }
    }

    private String generateSecureStringForUseAsAESKey() {
     return SecureRandomStringGenerator.generateSecureRandomString(64);
    }

    public void setClientAndSiteName(String clientName, String siteName) {
        addStringToLineStartingWith(clientName, "Owner");
        addStringToLineStartingWith(siteName, "Site-ID");
    }

    public void setServerAddress(String serverAddress) {
        addStringToLineStartingWith(serverAddress, "ServerURL");
    }

    public void writeFile(String outputFilePath) {
        try {
            Files.write(Paths.get(outputFilePath), outputFileLines);
        } catch (IOException e) {
            throw new RuntimeException("Could not write output file to " + outputFilePath);
        }
    }

    public String getAesString() {
        return aesString;
    }
}
