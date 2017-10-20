package сom.viktor.yurlov.repository;

import java.util.Set;
import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.RolePermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends CrudRepository<RolePermission, String> {
	@Query("select rp.permission from RolePermission rp where rp.role = :role")
	Set<Permission> findRolePermissions(@Param("role") Role role);
	Set<RolePermission> findRolePermissionByRole(Role role);
	Set<RolePermission> findRolePermissionByPermission(Permission permission);
}