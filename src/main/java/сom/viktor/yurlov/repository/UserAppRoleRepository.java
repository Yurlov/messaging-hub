package сom.viktor.yurlov.repository;

import java.util.Set;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.domain.UserAppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserAppRoleRepository extends JpaRepository<UserAppRole, String> {
	@Query("select ua.app from UserAppRole ua where ua.user = :user")
	Set<App> findAppsByUser(@Param("user") User user);

	@Query("select ua.role from UserAppRole ua where ua.user = :user and ua.app = :app")
	Set<Role> findRolesByUserApp(@Param("user") User user, @Param("app") App app);

	@Query("select ua.user from UserAppRole ua where ua.app = :app and ua.role = :role")
	Set<User> findUsersByAppRole(@Param("app") App app, @Param("role") Role role);

	@Query("select ua.user from UserAppRole ua where ua.app = :app")
	Set<User> findUsersByApp(@Param("app") App app);

	@Query("select ua.user from UserAppRole ua where ua.role = :role")
	Set<User> findUsersByRole(@Param("role") Role role);

	Set<UserAppRole> findUserAppRoleByUser(User user);

	Set<UserAppRole> findUserAppRoleByApp(App app);

	Set<UserAppRole> findUserAppRoleByAppAndUser(App app, User user);

	Set<UserAppRole> findUserAppRoleByRole(Role role);

	Set<UserAppRole> findAppByRoleAndUser(Role role, User user);
}