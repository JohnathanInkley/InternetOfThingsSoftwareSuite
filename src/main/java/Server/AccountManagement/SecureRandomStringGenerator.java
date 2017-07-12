package Server.AccountManagement;

import java.security.SecureRandom;
import java.util.Random;

public class SecureRandomStringGenerator {

    public static String generateSecureRandomString(int length) {
        char[] acceptableChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        SecureRandom srand = new SecureRandom();
        Random rand = new Random();
        char[] buff = new char[length];

        for (int i = 0; i < length; ++i) {
            if ((i % 10) == 0) {
                rand.setSeed(srand.nextLong());
            }
            buff[i] = acceptableChars[rand.nextInt(acceptableChars.length)];
        }
        return new String(buff);
    }
}
