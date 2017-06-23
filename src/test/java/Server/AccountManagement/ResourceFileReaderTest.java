package Server.AccountManagement;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResourceFileReaderTest {

    private static ResourceFileReader underTest;
    private static String directory;

    @BeforeClass
    public static void setUpUnderTest() {
        directory = "src/test/java/Server/AccountManagement/Resources/";
        underTest = new ResourceFileReader(directory,".txt");
    }

    @Test
    public void producesAnArrayOfAllTextFilesInDirectory() {
        HashMap<String, File> filesFound = underTest.getListOfFiles();
        assertEquals(directory + "0-test.txt", filesFound.get("0").toString());
        assertEquals(directory + "1-test.txt", filesFound.get("1").toString());
        assertEquals(2, filesFound.size());
    }

    @Test
    public void shouldBeAbleToGetFileContentsByNumber() {
        assertEquals("This is test0", underTest.getTextLinesForResource("0").get(0));
        assertEquals("This is test1", underTest.getTextLinesForResource("1").get(0));
    }

    @Test
    public void attemptingToGetResourceNotExistingThrowsException() {
        try {
            underTest.getTextLinesForResource("2");
            fail("Should only have files indexed 0 and 1");
        } catch (Exception e) {
            assertEquals("Resource file 2 could not be read. Please check files and try again", e.getMessage());
        }
    }

    @Test
    public void badDirectoryShouldThrowException() {
        try {
            ResourceFileReader badReader = new ResourceFileReader("badDirectory", ".txt");
            fail("No directory with this name");
        } catch (Exception e) {
            assertEquals("badDirectory is not a valid directory", e.getMessage());
        }
    }


}