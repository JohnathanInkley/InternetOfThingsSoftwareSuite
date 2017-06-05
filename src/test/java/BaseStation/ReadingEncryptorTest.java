package BaseStation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingEncryptorTest {

    @Test
    public void shouldEncryptSimpleMessage() {
        ReadingEncryptor underTest = new ReadingEncryptor();
        underTest.readKey("src/main/java/BaseStation/AESKey.txt");
        String encodedMessage = underTest.encrypt("hello world!");
        assertEquals("hello world!", underTest.decrypt(encodedMessage));
    }

}