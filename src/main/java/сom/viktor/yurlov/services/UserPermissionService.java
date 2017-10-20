package сom.viktor.yurlov.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.repository.RolePermissionRepository;
import сom.viktor.yurlov.repository.UserAppRoleRepository;
import org.springframework.stereotype.Service;


@Service
public class UserPermissionService {
    private final UserAppRoleRepository userAppRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public UserPermissionService(UserAppRoleRepository userAppRoleRepository,
                                 RolePermissionRepository rolePermissionRepository) {

        this.userAppRoleRepository = userAppRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public Map<Role, List<Permission>> getUserAppPermissions(User user, App app) {
        return userAppRoleRepository.findRolesByUserApp(user, app).stream()
                .collect(Collectors.toMap(role -> role, this::getPermissions));
    }

    public List<Permission> getPermissions(Role r) {
        List<Permission> result = new LinkedList<>();
        result.addAll(rolePermissionRepository.findRolePermissions(r));
        while (r.getParent() != null) {
            r = r.getParent();
            result.addAll(rolePermissionRepository.findRolePermissions(r));
        }
        return result;
    }

    public Map<App, Map<Role, List<Permission>>> getUserPermissions(User user) {
        return userAppRoleRepository.findAppsByUser(user).stream()
                .collect(Collectors.toMap(app -> app, app -> getUserAppPermissions(user, app)));
    }
}
