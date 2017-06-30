package Server.AccountManagement;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class UserConfigFileGeneratorTest {

    @Test
    public void shouldWriteFileWithGivenName() throws IOException {
        UserConfigFileGenerator underTest = new UserConfigFileGenerator();
        underTest.initialiseNewConfigFile();
        String outputFilePath = "src/test/java/Server/AccountManagement/userConfigTest.config";
        underTest.generateOutputFile(outputFilePath);

        assertTrue(Files.isRegularFile(Paths.get(outputFilePath)));

        Files.delete(Paths.get(outputFilePath));
    }

    @Test
    public void shouldBeAbleToAddUserNamePassWordPairAndHaveItWrittenToFile() throws IOException {
        UserConfigFileGenerator underTest = new UserConfigFileGenerator();
        underTest.initialiseNewConfigFile();
        underTest.addUser(new UsernamePasswordPair("user1", "password1"));
        underTest.addUser(new UsernamePasswordPair("user2", "password2"));

        String outputFilePath = "src/test/java/Server/AccountManagement/userConfigTest.config";
        underTest.generateOutputFile(outputFilePath);

        List<String> outputFileLines = Files.readAllLines(Paths.get(outputFilePath));

        assertTrue(outputFileLines.contains("username: user1 password: password1"));
        assertTrue(outputFileLines.contains("username: user2 password: password2"));
        assertEquals(2, outputFileLines.size());

        Files.delete(Paths.get(outputFilePath));
    }
}