package сom.viktor.yurlov.domain;

import javax.persistence.*;

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
public class App {
	@Id
	String id;

	@Column(length = 100, nullable = false, unique = true)
	String name;

	@ManyToOne(optional = false)
	Role defaultRole;
}
