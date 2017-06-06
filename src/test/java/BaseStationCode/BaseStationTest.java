package BaseStationCode;

import org.junit.Test;

import static org.junit.Assert.fail;

public class BaseStationTest {

    @Test
    public void incorrectConfigFileShouldCauseException() {
        try {
            BaseStation underTest = new BaseStation();
            underTest.readConfigFile("noFileCalledThis");
            fail("Should throw exception with bad file name");
        } catch (RuntimeException e) {
            // Test passed if reach here
        }
    }

    @Test
    public void correctFileShouldNotCauseException() {
        try {
            BaseStation underTest = new BaseStation();
            underTest.readConfigFile("src/main/java/BaseStationCode/Resources/ownerFactoryBaseStation.config");
        } catch (RuntimeException e) {
            fail("Should not throw exception with correctly laid out file");
        }
    }

}