package сom.viktor.yurlov.util;

import java.util.UUID;

public class UuidGeneratorUtil {

    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}