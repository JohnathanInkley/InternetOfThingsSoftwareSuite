package Server.BaseStationServerStuff;

import BaseStationCode.BaseStation;
import BaseStationCode.ReadingEncryptor;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IncomingReadingDecryptorTest {

    @Test
    public void shouldDecodeMessageCorrectlyBasedOnDeviceCollection() throws IOException {
        IncomingReadingDecryptor underTest = new IncomingReadingDecryptor();
        underTest.readInKeysFromFile("src/main/java/Server/BaseStationServerStuff/Resources/OwnerKeys.txt");

        String messageToCheck = encryptMessage("[{time,,2000-01-01 12:34:56.123}]");

        assertEquals("[{time,,2000-01-01 12:34:56.123}]", underTest.decrypt(messageToCheck));
    }

    private String encryptMessage(String message) {
        String ownerFactoryKey = "4}ilu|y<(7)7$vDuwRpoy[:1FeC5Z-IN&jy`]^fy~6TL<%v}5-QVk8,@tB=gPb~7";
        ReadingEncryptor encryptor = new ReadingEncryptor(ownerFactoryKey);
        String messageToCheck = "owner.factory"
                + BaseStation.DELIMITER_TO_SEPARATE_MESSAGE_AND_OWNER
                + encryptor.encrypt(message);
        return messageToCheck;
    }

    @Test
    public void shouldThrowExceptionWhenFileDoesNotExist() {
        try {
            IncomingReadingDecryptor underTest = new IncomingReadingDecryptor();
            underTest.readInKeysFromFile("badPath");
            fail("Exception should be thrown in key file not exist");
        } catch (Exception e) {
            // Should reach here
        }
    }
}