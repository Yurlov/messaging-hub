package сom.viktor.yurlov.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
public class Permission {
	@Id
	String id;

	@Column(length = 20)
	String name;

	@Column(nullable = false, columnDefinition = "tinyint(1) default 1")
	Boolean allow;

	@Column(length = 50)
	String action;

	String resource;
}
