package BaseStationCode;

import Server.BaseStationServerStuff.SensorReadingParser;
import Server.DatabaseStuff.DatabaseEntry;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaseStationManager {

    private static final int PRODUCER_WAIT_TIME_IN_MS = 100;
    private static final int BACKUP_WAIT_TIME_IN_MS = 1000;

    private SensorReadingHandler handler;
    private int handlerSizeLimit;
    private SensorReadingHandler handlerToBackUp;
    private boolean backupFlag = false;

    private SensorReadingProducer producer;
    private SensorReadingSender consumer;
    private SensorReadingBackupCreator backupCreator;
    private ReadingEncryptor encryptor;
    private SensorReadingParser parser;
    private DBEntryToStringConverter entryToStringConverter;

    private boolean shutdown = false;
    private String collectionID = "";


    public void setHandler(SensorReadingHandler handler, int handlerSizeLimit) {
        this.handler = handler;
        this.handlerSizeLimit = handlerSizeLimit;
    }

    public void setProducer(SensorReadingProducer producer) {
        this.producer = producer;
    }

    public void setConsumer(SensorReadingSender consumer) {
        this.consumer = consumer;
    }

    public void setBackupCreator(SensorReadingBackupCreator backupCreator) {
        this.backupCreator = backupCreator;
    }

    public void setEncryptor(ReadingEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public void setDeviceCollection(String collectionID) {
        this.collectionID = collectionID;
    }

    public void setReadingParser(SensorReadingParser parser) {
        this.parser = parser;
    }

    public void setEntryToStringConverter(DBEntryToStringConverter entryToStringConverter) {
        this.entryToStringConverter = entryToStringConverter;
    }

    public void start() {
        ExecutorService service = Executors.newFixedThreadPool(3);
        service.execute(this::getReadingsFromProducerAndAddToHandler);
        service.execute(this::getReadingsFromHandlerAndPassToConsumer);
        service.execute(this::createBackupsPeriodicallyIfNeeded);
        service.shutdown();
    }

    private void createBackupsPeriodicallyIfNeeded() {
        while (!shutdown) {
            putInEmptyHandlerIfHandlerFull();
            writeBackupHandlerToFile();
            waitForTimeInMS(BACKUP_WAIT_TIME_IN_MS);
            readBackupFromFileIfHandlerEmpty();
            waitForTimeInMS(BACKUP_WAIT_TIME_IN_MS);
        }
    }

    private synchronized void putInEmptyHandlerIfHandlerFull() {
        if (handler.size() >= handlerSizeLimit) {
            handlerToBackUp = handler;
            handler = new SensorReadingHandler();
            backupFlag = true;
        }
    }

    private void writeBackupHandlerToFile() {
        if (backupFlag) {
            backupCreator.write(handlerToBackUp);
            backupFlag = false;
        }
    }

    private void waitForTimeInMS(int timeInMs) {
        try {
            Thread.sleep(timeInMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void readBackupFromFileIfHandlerEmpty() {
        if (handler.isEmpty() && backupCreator.getNumberOfBackups() > 0) {
            handler = backupCreator.read();
            this.notifyAll();
        }
    }

    private void getReadingsFromProducerAndAddToHandler() {
        while (!shutdown) {
            String message = producer.getSingleReading();
            if (!message.equals("")) {
                addMessageToHandler(message);
            } else {
                waitForTimeInMS(PRODUCER_WAIT_TIME_IN_MS);
            }
        }
    }

    private synchronized void addMessageToHandler(String message) {
        handler.add(message);
        this.notifyAll();
    }

    private void getReadingsFromHandlerAndPassToConsumer() {
        try {
            while (!shutdown) {
                String rawReading = waitUntilHandlerHasReadingThenGet();
                DatabaseEntry parsedEntry = parser.parseReading(rawReading);
                String entryAsString = entryToStringConverter.convertToString(parsedEntry);
                String encryptedEntryAsString = encryptor.encrypt(entryAsString);
                String fullMessage = collectionID + BaseStation.DELIMITER_TO_SEPARATE_MESSAGE_AND_OWNER + encryptedEntryAsString;
                consumer.send(fullMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized String waitUntilHandlerHasReadingThenGet() throws InterruptedException {
        while (handler.isEmpty() && !shutdown) {
            this.wait();
        }
        return handler.get();
    }

    public void join() {
        shutdown = true;
        backupCreator.removeBackups();
    }
}
