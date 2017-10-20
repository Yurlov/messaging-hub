package сom.viktor.yurlov.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
	@Id
	String id;

	@JsonProperty("username")
	@Column(name = "username", length = 30, nullable = false, unique = true)
	@NotNull
	String userName;

	@JsonProperty("first_name")
	@Column(name = "first_name", length = 30, nullable = false)
	@NotNull
	String firstName;

	@JsonProperty("last_name")
	@Column(name = "last_name", length = 30, nullable = false)
	@NotNull
	String lastName;

	@Column(length = 20, unique = true)
	String mobile;

	String address;

	@Column(unique = true)
	String email;

	@JsonIgnore
	@Column(length = 1000, nullable = false)
	String password;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "ENUM('enabled', 'disabled', 'pending')")
	UserStatus status;

	public enum UserStatus {
		enabled, disabled, pending
	}

	public User() {
	}

	public User(String name, String firstName, String lastName, String password) {
		this.userName = name;
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
	}
}