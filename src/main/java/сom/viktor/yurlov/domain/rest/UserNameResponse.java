package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserNameResponse {
    @JsonProperty("username")
    String username;
    public UserNameResponse(String username) {
        this.username = username;
    }
}
