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
public class ResetPasswordRequest {

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Body {
		@JsonProperty("user_id")
		@NotNull
		String userId;

		String url; // allow customer to build their own reset password form, overwrite iam.address

		@JsonProperty("by_link")
		boolean byLink; // whether to use link instead of code
	}
	@NotNull
	@Valid
	Body body;
    @JsonRawValue
    String extra;
}
