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
public class ResetPasswordConfirmRequest {
	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Secure {
		@JsonProperty("new_password")
		@NotNull
		String newPassword;
	}

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Body {
		@JsonProperty("receiver")
		String receiver; // email or mobile

		@JsonProperty("code")
		String code;
	}

    @JsonRawValue
    String extra;
    @NotNull
    @Valid
    Secure secure;

	@Valid
	Body body;
}
