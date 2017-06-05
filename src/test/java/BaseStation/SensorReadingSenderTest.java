package BaseStation;

import Server.BaseStationServerStuff.BaseStationConnectionServer;
import Server.BaseStationServerStuff.SensorReadingParser;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class SensorReadingSenderTest {

    @Test
    public void invalidURLShouldReturnFalseWhenMessageSent() {
        SensorReadingSender underTest = new SensorReadingSender("badURL");
        assertFalse(underTest.send("message"));
    }

    @Test
    public void validURLShouldReturnTrueWhenMessageSent() {
        BaseStationConnectionServer server = new BaseStationConnectionServer("http://localhost:8080/SensorServer");
        server.setReadingParser(new SensorReadingParser());
        ReadingEncryptor encryptor = new ReadingEncryptor();
        encryptor.readKey("src/main/java/BaseStation/AESKey.txt");
        server.setEncryptor(encryptor);
        server.runServer();

        SensorReadingSender underTest = new SensorReadingSender("http://localhost:8080/SensorServer/server");
        assertTrue(underTest.send("message"));

        server.stopServer();
    }


}