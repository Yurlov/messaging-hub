package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonRawValue;
import сom.viktor.yurlov.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class UpdateAccountRequest {
    User body;
    @JsonRawValue
    String extra;
}
