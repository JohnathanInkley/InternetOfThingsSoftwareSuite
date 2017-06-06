package BaseStationCode;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReadingEncryptorTest {

    @Test
    public void shouldEncryptSimpleMessage() {
        ReadingEncryptor underTest = new ReadingEncryptor("4}ilu|y<(7)7$vDuwRpoy[:1FeC5Z-IN&jy`]^fy~6TL<%v}5-QVk8,@tB=gPb~7");
        String encodedMessage = underTest.encrypt("hello world!");
        assertEquals("hello world!", underTest.decrypt(encodedMessage));
    }

}