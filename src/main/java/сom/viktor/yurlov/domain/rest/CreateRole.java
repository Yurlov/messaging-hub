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
import java.util.List;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateRole {
	@NotNull
	@Valid
	CreateRoleBody body;
    @JsonRawValue
    String extra;

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
	@Setter
	@Accessors(chain = true)
	@EqualsAndHashCode
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public final static class CreateRoleBody {
		@NotNull
		String name;

		String parent;

		String description;

		List<String> permissions;
	}
}