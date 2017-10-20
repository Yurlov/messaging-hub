package сom.viktor.yurlov.domain;


import javax.persistence.Column;
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
public class Role {
    @Id
    String id;

    @Column(length = 20, unique = true, nullable = false)
    String name;

    @ManyToOne
    Role parent;

    String description;
}