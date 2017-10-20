package сom.viktor.yurlov.domain.rest;

import сom.viktor.yurlov.util.AES;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true, fluent = true)
public class Code {
    public static final String ACTIVATION = "ACT";
    public static final String PASSWORD = "PWD";

    String type;
    String userId;
    long timestamp;

    public Code(String type, String userId) {
        this.type = type;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    public Code() { }

    public String encrypt(String secretKey) {
        return AES.encrypt(type + "&" + userId + "&" + timestamp, secretKey);
    }

    public static Code decrypt(String code, String secretKey) {
        String decrypted = AES.decrypt(code, secretKey);
        if (decrypted == null) return null;
        String[] parts = decrypted.split("&");
        return new Code().type(parts[0]).userId(parts[1]).timestamp(Long.parseLong(parts[2]));
    }
}
