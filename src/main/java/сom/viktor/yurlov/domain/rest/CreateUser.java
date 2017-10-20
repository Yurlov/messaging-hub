package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import сom.viktor.yurlov.domain.User;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUser {
	@NotNull
	@Valid
	CreateUserBody body;
    @JsonRawValue
    String extra;

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
	@Setter
	@Accessors(chain = true)
	@EqualsAndHashCode
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public final static class CreateUserBody {
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

		String password;

		User.UserStatus status;
	}
}