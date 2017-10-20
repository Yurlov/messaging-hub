package сom.viktor.yurlov.repository;

import java.util.Set;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AppRepository extends JpaRepository<App, String> {
	App findByName(String name);
	Set<App> findByDefaultRole(Role role);
}