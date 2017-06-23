package Server.AccountManagement;

import Server.AccountManagement.Resources.Handler1;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UserChoiceHandlerGeneratorTest {

    private static UserChoiceHandlerGenerator underTest;

    @BeforeClass
    public static void setUpUnderTest() {
        String configFile = "src/test/java/Server/AccountManagement/Resources/dummyHandlerClassLookup.config";
        underTest = new UserChoiceHandlerGenerator(configFile);

    }

    @Test
    public void shouldReturnHandlerOfCorrectClassForUserChoice() {
        assertTrue(Handler1.class.isInstance(underTest.getHandler("1")));
    }

    @Test
    public void shouldThrowExceptionWhenUserChoiceNotFound() {
        try {
            underTest.getHandler("2");
            fail("No handler specified for this choice in config file");
        } catch (Exception e) {
            assertEquals("Choice 2 not specified in config file", e.getMessage());
        }
    }

    @Test
    public void shouldThrowExceptionWhenChoiceMentionedInConfigFileButClassNotThere() {
        try {
            underTest.getHandler("badLookup");
            fail("Class name specified in config file is nonsense");
        } catch (Exception e) {
            assertEquals("Cannot find handler for badLookup (user choice)", e.getMessage());
        }
    }
}

