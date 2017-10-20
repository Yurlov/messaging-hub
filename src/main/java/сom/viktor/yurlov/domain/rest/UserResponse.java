package сom.viktor.yurlov.domain.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse extends User {

    List<UserApp> apps = new ArrayList<>();

    public UserResponse(User user, Map<App, Map<Role, List<Permission>>> roles) {
        for (Field f : User.class.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                f.set(this, f.get(user));
            } catch (Exception ignored) {
            }
        }

        roles.forEach((app, role) -> apps.add(new UserApp()
                .setApp(app.getName())
                .setRoles(role.entrySet().stream().map(roleEntry -> new UserRole()
                        .setPermissions(roleEntry.getValue().stream().map(permission -> permission.setId(null)).collect(Collectors.toList()))
                        .setName(roleEntry.getKey().getName()))
                        .collect(Collectors.toList()))));
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserApp {
        String app;
        List<UserRole> roles;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserRole {
        String name;
        List<Permission> permissions;
    }
}