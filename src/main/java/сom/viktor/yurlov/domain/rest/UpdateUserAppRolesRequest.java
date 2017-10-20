package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserAppRolesRequest {
	@NotNull
	@Valid
	Body body;
    @JsonRawValue
    String extra;

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
	@Setter
	@Accessors(chain = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public final static class Body {
		@NotNull
		String username;

		@NotNull
		String app_name;

		@NotNull
		String[] roles;
	}
}