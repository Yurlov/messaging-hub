package сom.viktor.yurlov.domain.rest;

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
public class VerifyRequest {


    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Accessors(chain = true)
    public static class Body {
        @NotNull
        String receiver;
    }

    @NotNull
    @Valid
    Body body;
    @JsonRawValue
    String extra;
}
