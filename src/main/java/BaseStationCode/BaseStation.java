package BaseStationCode;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class BaseStation {

    public static final String DELIMITER_TO_SEPARATE_MESSAGE_AND_OWNER = "!";
    public static final String DELIMITED_BETWEEN_OWNER_AND_SITE_ID = ".";

    private Map<String, String> configMap;
    private String owner;
    private String siteID;
    private String AESKey;
    private String serverURL;
    private String deviceCollection;
    private int handlerSizeLimit;

    private BaseStationManager threadRunner;
    private SensorReadingHandler handler;
    private SensorReadingProducer producer;
    private SensorReadingSender sender;
    private SensorReadingBackupCreator backupCreator;
    private ReadingEncryptor readingEncryptor;

    public void readConfigFile(String configFileName) {
        try (Stream<String> configItems = Files.lines(Paths.get(configFileName))) {
            generateConfigMap(configItems);
            setClassParameters();
        } catch (Exception e) {
            throw new RuntimeException("Config file could not be read, please check file is correctly laid out" +
                    "as shown is sample file provided.\nAborting application");
        }
    }

    private void generateConfigMap( Stream<String> configItems) {
        configMap = new HashMap<>();
        configItems.forEach((item) -> {
            String[] itemWithKeyAndValue = item.split("\\s+");
            configMap.put(itemWithKeyAndValue[0], itemWithKeyAndValue[1]);
        });
    }

    private void setClassParameters() {
        owner = configMap.get("Owner");
        siteID = configMap.get("Site-ID");
        AESKey = configMap.get("AES-Key");
        serverURL = configMap.get("ServerURL");
        handlerSizeLimit = Integer.parseInt(configMap.get("BackUpReadingsWhenCountReached"));
        deviceCollection= owner + DELIMITED_BETWEEN_OWNER_AND_SITE_ID + siteID;
    }

    public void initialiseComponents() {
        setUpEncryptor();
        setUpBackupCreator();
        setUpSender();
        setUpProducer();
        setUpHandler();
        setUpThreadRunner();

    }

    private void setUpEncryptor() {
        readingEncryptor = new ReadingEncryptor(AESKey);
    }

    private void setUpBackupCreator() {
        backupCreator = new SensorReadingBackupCreator(owner + siteID);
    }

    private void setUpSender() {
        sender = new SensorReadingSender(serverURL);
    }

    private void setUpProducer() {
        producer = new SensorReadingProducer(deviceCollection);
        producer.findPortAndOpen();
    }

    private void setUpHandler() {
        handler = new SensorReadingHandler();
    }


    private void setUpThreadRunner() {
        threadRunner = new BaseStationManager();
        threadRunner.setEncryptor(readingEncryptor);
        threadRunner.setBackupCreator(backupCreator);
        threadRunner.setConsumer(sender);
        threadRunner.setHandler(handler, handlerSizeLimit);
        threadRunner.setProducer(producer);
        threadRunner.setDeviceCollection(deviceCollection);
    }

    public void start() {
        threadRunner.start();
    }

    public void stop() {
        threadRunner.join();
    }
}
