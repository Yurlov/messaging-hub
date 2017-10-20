package сom.viktor.yurlov.repository;

import сom.viktor.yurlov.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, String> {
	Role findByName(String name);
}