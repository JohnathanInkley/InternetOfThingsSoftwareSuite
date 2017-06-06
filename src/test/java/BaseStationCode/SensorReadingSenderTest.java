package BaseStationCode;

import Server.BaseStationServerStuff.BaseStationConnectionServer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public class SensorReadingSenderTest {

    @Test
    public void invalidURLShouldReturnFalseWhenMessageSent() throws InterruptedException {
        SensorReadingSender underTest = new SensorReadingSender("badURL");

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Collection<Callable<Boolean>> callables = new ArrayList<>();
        Future<Boolean> result = executorService.submit(() -> underTest.send("message"));

        Thread.sleep(5000);

        if (result.isDone()) {
            fail("send should not have returned yet");
        }

        result.cancel(true);
        executorService.shutdownNow();
    }

    @Test
    public void validURLShouldReturnTrueWhenMessageSent() throws InterruptedException {
        SensorReadingSender underTest = new SensorReadingSender("http://localhost:8080/SensorServer/server");
        BaseStationConnectionServer server = new BaseStationConnectionServer("http://localhost:8080/SensorServer");
        server.runServer();

        assertTrue(underTest.send("message"));

        server.stopServer();
    }


}