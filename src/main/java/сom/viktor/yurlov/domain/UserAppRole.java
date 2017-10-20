package сom.viktor.yurlov.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@Accessors(chain = true)
public class UserAppRole {
	@Id
	String id;

	@ManyToOne(optional = false)
	User user;

	@ManyToOne(optional = false)
	App app;

	@ManyToOne(optional = false)
	Role role;
}