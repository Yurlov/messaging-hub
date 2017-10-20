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
public class RolePermission {
	@Id
	String id;

	@ManyToOne(optional = false)
	Role role;

	@ManyToOne(optional = false)
	Permission permission;
}