package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePermission {
	@NotNull
	@Valid
	CreatePermissionBody body;
    @JsonRawValue
    String extra;

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
	@Setter
	@Accessors(chain = true)
	@EqualsAndHashCode
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public final static class CreatePermissionBody {
		String id;
		@NotNull
		String name;
		@NotNull
		Boolean allow;
		@NotNull
		String action;
		@NotNull
		String resource;
	}
}