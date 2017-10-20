package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class SendMessageRequest {

    public static final String MSG_TYPE_MAIL = "MAIL";
    public static final String MSG_TYPE_SMS = "SMS";

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static class Body {
        List<String> receivers;
        String type;
        String message;
    }

    Body body;

    @JsonRawValue
    String extra;

    public SendMessageRequest() { }

    public SendMessageRequest(String receiver, String type, String message) {
        this.body = new Body()
            .setReceivers(Collections.singletonList(receiver))
            .setType(type).setMessage(message);
    }
}
