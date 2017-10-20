package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class JoinAppRequest {

    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static class Body {
        @JsonProperty("app_name")
        String appName;
    }

    @JsonRawValue
    String extra;
    Body body;
}
