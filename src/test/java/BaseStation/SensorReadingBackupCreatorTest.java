package BaseStation;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class SensorReadingBackupCreatorTest {

    @Test
    public void shouldWriteToFilesWithAGivenPrefix() {
        SensorReadingBackupCreator underTest = new SensorReadingBackupCreator("testPrefix");
        SensorReadingHandler handler = new SensorReadingHandler();
        handler.add("test");

        underTest.write(handler);
        underTest.write(handler);

        SensorReadingHandler readHandler1 = underTest.read();
        SensorReadingHandler readHandler2 = underTest.read();

        String message1 = readHandler1.get();
        String message2 = readHandler2.get();

        assertEquals("test", message1);
        assertEquals("test", message2);

        underTest.removeBackups();
    }

    @Test
    public void shouldKeepTrackOfNumberOfBackups() {
        SensorReadingBackupCreator underTest = new SensorReadingBackupCreator("testPrefix");
        SensorReadingHandler handler = new SensorReadingHandler();
        underTest.write(handler);

        assertEquals(1, underTest.getNumberOfBackups());

        underTest.removeBackups();
    }

    @Test
    public void ifBackupsWithSameNameExistWillBeReadIn() {
        SensorReadingBackupCreator backupCreatorSomehowCrashes = new SensorReadingBackupCreator("previousBackupRun");
        SensorReadingHandler handler = new SensorReadingHandler();
        handler.add("test");
        backupCreatorSomehowCrashes.write(handler);

        SensorReadingBackupCreator underTest = new SensorReadingBackupCreator("previousBackupRun");
        SensorReadingHandler handlerGotAfterCrash = underTest.read();
        assertEquals("test", handler.get());

        underTest.removeBackups();
    }
}