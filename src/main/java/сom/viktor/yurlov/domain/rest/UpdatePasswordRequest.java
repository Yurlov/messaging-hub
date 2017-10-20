package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_NULL)
public class UpdatePasswordRequest {

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Secure {
		@JsonProperty("new_password")
		@NotNull
		String newPassword;
		@JsonProperty("old_password")
		@NotNull
		String oldPassword;
	}

	@NotNull
	@Valid
	Secure secure;
    @JsonRawValue
    String extra;
}