package сom.viktor.yurlov.repository;

import java.util.List;
import сom.viktor.yurlov.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface UserRepository extends JpaRepository<User, String> {
	User findByUserName(String userName);

	@Query("select u from User u where u.userName = :userId or u.email = :userId or u.mobile = :userId")
	User findByUserId(@Param("userId") String userId);

	List<User> findByMobile(String mobile);

	List<User> findByEmail(String email);
}