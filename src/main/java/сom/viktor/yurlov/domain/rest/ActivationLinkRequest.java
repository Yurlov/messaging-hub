package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class ActivationLinkRequest {


    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static class Body {
        @JsonProperty("user_id")
        @NotNull
        String userId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    @JsonRawValue
    String extra;

    @NotNull
    @Valid
    Body body;

}
