package сom.viktor.yurlov.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import org.apache.tomcat.util.buf.HexUtils;
import static org.apache.tomcat.util.buf.HexUtils.fromHexString;
import static org.apache.tomcat.util.buf.HexUtils.toHexString;


public class AES {

    private static SecretKeySpec getKey(String myKey) {
        try {
            byte[] key = myKey.getBytes("UTF-8");
            key = MessageDigest.getInstance("SHA-1").digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String strToEncrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(secret));
            return toHexString(Base64.getEncoder().encode(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            return null;
        }
    }

    public static String decrypt(String strToDecrypt, String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, getKey(secret));
            return new String(cipher.doFinal(Base64.getDecoder().decode(fromHexString(strToDecrypt))));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            return null;
        }
    }
}