package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.util.BeanUtil;
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
public class RegisterRequest {

	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Secure {
		@NotNull
		String password;
	}


	@Getter
	@Setter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	public static class Body {
		String id;

		@JsonProperty("username")
		@NotNull
		String userName;

		@JsonProperty("first_name")
		@NotNull
		String firstName;

		@JsonProperty("last_name")
		@NotNull
		String lastName;

		String mobile;

		String address;

		String email;

		@JsonProperty("verification_code")
		String verificationCode;
	}

	@NotNull
	@Valid
	Body body;

    @JsonRawValue
    String extra;
    @NotNull
	@Valid
	Secure secure;

	public User getUser() {
		User user = new User();
		BeanUtil.copySameProperties(body, user);
		return user;
	}
}
