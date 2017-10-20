package сom.viktor.yurlov.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA {
    public static String encrypt(String strToEncrypt) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((strToEncrypt).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }
}
