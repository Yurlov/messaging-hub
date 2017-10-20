package сom.viktor.yurlov.repository;

import сom.viktor.yurlov.domain.Permission;
import org.springframework.data.repository.CrudRepository;


public interface PermissionRepository extends CrudRepository<Permission, String> {
	Permission findByName(String name);
}