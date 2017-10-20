package сom.viktor.yurlov.util;

import org.apache.commons.lang3.RandomStringUtils;

public class PasswordUtil {

    public static String encrypt(String password) {
        String salt = RandomStringUtils.randomAlphanumeric(8);
        String encrypted = SHA.encrypt(password + salt);
        return encrypted + "." + salt;
    }

    public static boolean compare(String encryptedTrue, String rawPossible) {
        String truePassword = encryptedTrue.split("\\.")[0];
        String salt = encryptedTrue.split("\\.")[1];
        String encryptedPass = SHA.encrypt(rawPossible + salt);
        return truePassword.equals(encryptedPass);
    }
}
