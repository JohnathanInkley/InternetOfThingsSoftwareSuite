package BaseStation;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class ReadingEncryptor {

    private String keyFileName;
    private byte[] rawKeyAsBites;
    private SecretKey secretAESKey;
    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    public void readKey(String keyFileName) {
        this.keyFileName = keyFileName;
        readKeyBytesFromFile();
        generateAESKey();
        initialiseCiphers();

    }

    private void readKeyBytesFromFile() {
        try {
            rawKeyAsBites = Files.readAllBytes(Paths.get(keyFileName));
        } catch (Exception e) {
            throw new RuntimeException("AES secretAESKey could not be read and generated! Please check file and try again");
        }
    }

    private void generateAESKey() {
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-1");
            byte[] hashedText = shaDigest.digest(rawKeyAsBites);
            byte[] hashedText128bits = Arrays.copyOf(hashedText, 16);
            secretAESKey = new SecretKeySpec(hashedText128bits, "AES");
        } catch (Exception e) {
            throw new RuntimeException("AES secretAESKey could not be generated");
        }
    }

    private void initialiseCiphers() {
        try {
            encryptionCipher = Cipher.getInstance("AES");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, secretAESKey);
            decryptionCipher = Cipher.getInstance("AES");
            decryptionCipher.init(Cipher.DECRYPT_MODE, secretAESKey);
        } catch (Exception e) {
            throw new RuntimeException("Ciphers could not be made from key");
        }
    }

    public String encrypt(String message) {
        try {
            byte[] encryptedBytes = encryptionCipher.doFinal(message.getBytes());
            byte[] base64Bytes = Base64.getEncoder().encode(encryptedBytes);
            return new String(base64Bytes);
        } catch (Exception e) {
           throw new RuntimeException("Message could not be encoded");
        }
    }

    public String decrypt(String message) {
        try {
            byte[] base64message = Base64.getDecoder().decode(message.getBytes());
            return new String(decryptionCipher.doFinal(base64message));
        } catch (Exception e) {
            throw new RuntimeException("Message could not be decoded");
        }
    }
}
