package Server.AccountManagement;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class SiteConfigFileGeneratorTest {

    @Test
    public void shouldMakeConfigFileWithRightLineTitles() throws IOException {
        SiteConfigFileGenerator underTest = new SiteConfigFileGenerator("src/main/java/Server/AccountManagement/template.config");
        underTest.initialiseNewConfig();
        underTest.setClientAndSiteName("client", "site");
        underTest.setServerAddress("http://localhost:8080/SensorServer/server");
        underTest.writeFile("src/test/java/Server/AccountManagement/testFile.config");

        List<String> lines = Files.readAllLines(Paths.get("src/test/java/Server/AccountManagement/testFile.config"));
        assertEquals("Owner client", lines.get(0));
        assertEquals("Site-ID site", lines.get(1));
        assertTrue(lines.get(2).contains("AES-Key"));
        assertEquals(72, lines.get(2).length());
        assertEquals("ServerURL http://localhost:8080/SensorServer/server", lines.get(3));
        assertEquals("BackUpReadingsWhenCountReached 10", lines.get(4));

        Files.delete(Paths.get("src/test/java/Server/AccountManagement/testFile.config"));
    }

    @Test
    public void shouldThrowExceptionWhenBadTemplateFileGiven() {
        try {
            SiteConfigFileGenerator underTest = new SiteConfigFileGenerator("badFile");
            fail("No template file exists with this name");
        } catch (Exception e) {
            assertEquals("Could not open template config file: badFile", e.getMessage());
        }
    }
}