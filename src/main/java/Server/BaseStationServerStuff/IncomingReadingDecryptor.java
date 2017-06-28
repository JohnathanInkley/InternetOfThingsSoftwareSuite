package Server.BaseStationServerStuff;

import BaseStationCode.BaseStation;
import BaseStationCode.ReadingEncryptor;
import Server.DatabaseStuff.ClientDatabaseEditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class IncomingReadingDecryptor {

    public static final String UNAUTHORIZED_MESSAGE_ATTEMPT = "BAD_MESSAGE";
    private Map<String, String> keyMap;
    private Map<String, ReadingEncryptor> encryptorMap;

    public IncomingReadingDecryptor() {
        encryptorMap = new HashMap<>();
    }

    public void setKeyMap(Map<String,String> keyMap) {
        this.keyMap = keyMap;
        generateEncryptorMap();
    }

    public void readInKeysFromFile(String fileOfKeys) {
        try {
            keyMap = new HashMap<>();
            Stream<String> lines = Files.lines(Paths.get(fileOfKeys));
            lines.forEach((keyPair) -> {
                String[] collectionAndKey = keyPair.split("\\s+");
                keyMap.put(collectionAndKey[0], collectionAndKey[1]);
            });
            generateEncryptorMap();
        } catch (IOException e) {
            throw new RuntimeException("Key file could not be read. Server cannot start without encryption");
        }
    }

    public void getKeysFromDatabase(ClientDatabaseEditor editor) {
        setKeyMap(editor.getAesKeys());
        generateEncryptorMap();
    }

    private void generateEncryptorMap() {
        for (String deviceCollection : keyMap.keySet()) {
            ReadingEncryptor encryptor = new ReadingEncryptor(keyMap.get(deviceCollection));
            encryptorMap.put(deviceCollection, encryptor);
        }
    }

    public String decrypt(String messageToCheck) {
        String decryptedMessage = attemptToDecryptMessage(messageToCheck);
        return decryptedMessage;
    }

    private String attemptToDecryptMessage(String messageToCheck) {
        try {
            int splitPoint = messageToCheck.indexOf(BaseStation.DELIMITER_TO_SEPARATE_MESSAGE_AND_OWNER);
            String deviceCollection = messageToCheck.substring(0, splitPoint);
            String messageToDecrypt = messageToCheck.substring(splitPoint + 1, messageToCheck.length());
            return encryptorMap.get(deviceCollection).decrypt(messageToDecrypt);
        } catch (Exception e) {
            return UNAUTHORIZED_MESSAGE_ATTEMPT;
        }
    }
}
