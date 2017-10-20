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
public class LoginRequest {

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Body {
		@JsonProperty("user_id")
		@NotNull
		String userId;
	}

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Secure {
		@NotNull
		String password;
	}

	@NotNull
	@Valid
	Body body;
	@NotNull
	@Valid
	Secure secure;
    @JsonRawValue
    String extra;
}