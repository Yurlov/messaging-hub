package сom.viktor.yurlov;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.RolePermission;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.domain.UserAppRole;
import сom.viktor.yurlov.repository.AppRepository;
import сom.viktor.yurlov.repository.PermissionRepository;
import сom.viktor.yurlov.repository.RolePermissionRepository;
import сom.viktor.yurlov.repository.RoleRepository;
import сom.viktor.yurlov.repository.UserAppRoleRepository;
import сom.viktor.yurlov.repository.UserRepository;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;
import static сom.viktor.yurlov.domain.User.UserStatus.enabled;
import static сom.viktor.yurlov.util.PasswordUtil.encrypt;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.junit.Assert.assertEquals;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
@FieldDefaults(level = AccessLevel.PROTECTED)
public class MvcTestCase {
	final static String HEADER_USER_ID = "X-Hylaa-Username";

	@Autowired
	MockMvc mvc;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	AppRepository appRepository;

	@Autowired
    PermissionRepository permissionRepository;

	@Autowired
	RolePermissionRepository rolePermissionRepository;

	@Autowired
	UserAppRoleRepository userAppRoleRepository;

	@MockBean
	RestTemplate template;

	ObjectMapper mapper = new ObjectMapper();
	Role kingRole = new Role().setName("king").setId(UuidGeneratorUtil.generateUuid()).setDescription("no description");
	Permission permission1 = new Permission().setName("kill").setAllow(true).setId(UuidGeneratorUtil.generateUuid());
	Permission permission2 = new Permission().setName("capitalize").setAllow(true).setId(UuidGeneratorUtil.generateUuid());
	Permission permission3 = new Permission().setName("dance").setAllow(true).setId(UuidGeneratorUtil.generateUuid());
	App app1 = new App().setName("app1").setDefaultRole(kingRole).setId(UuidGeneratorUtil.generateUuid());
	App app2 = new App().setName("app2").setDefaultRole(kingRole).setId(UuidGeneratorUtil.generateUuid());
	App app3 = new App().setName("app3").setDefaultRole(kingRole).setId(UuidGeneratorUtil.generateUuid());

	protected void initJhonSnow() throws Exception {
		String userName = "Stark";
		setUpDb(userName, UserIdType.userName, "P@ssw0rd");

		User user = new User("ghost", "Jhon", "Snow", encrypt("pass")).setStatus(enabled).setId(UuidGeneratorUtil.generateUuid());

		userRepository.save(user);


		UserAppRole userAppRole1 = new UserAppRole().setRole(kingRole).setApp(app1).setUser(user).setId(UuidGeneratorUtil.generateUuid());
		UserAppRole userAppRole2 = new UserAppRole().setRole(kingRole).setApp(app2).setUser(user).setId(UuidGeneratorUtil.generateUuid());
		UserAppRole userAppRole3 = new UserAppRole().setRole(kingRole).setApp(app3).setUser(user).setId(UuidGeneratorUtil.generateUuid());
		roleRepository.save(kingRole);
		appRepository.save(asList(app1, app2, app3));
		userAppRoleRepository.save(asList(userAppRole1, userAppRole2, userAppRole3));


		permissionRepository.save(asList(permission1, permission2, permission3));
		RolePermission rolePermission1 = new RolePermission().setRole(kingRole).setPermission(permission1).setId(UuidGeneratorUtil.generateUuid());
		RolePermission rolePermission2 = new RolePermission().setRole(kingRole).setPermission(permission2).setId(UuidGeneratorUtil.generateUuid());
		RolePermission rolePermission3 = new RolePermission().setRole(kingRole).setPermission(permission3).setId(UuidGeneratorUtil.generateUuid());

		rolePermissionRepository.save(asList(rolePermission1, rolePermission2, rolePermission3));
	}

	@Setter
	@Getter
	@FieldDefaults(level = AccessLevel.PRIVATE)
	@Accessors(chain = true)
	protected static class Request<B, S> {
		B body;
		S secure;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	protected static class UserBody {
		String username;
		String mobile;
		String address;
		String email;
		String first_name;
		String last_name;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	protected static class LoginBody {
		String user_id;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	protected static class ResetBody {
		String user_id;
		boolean by_link;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	protected static class Secure {
		String password;
	}

	protected enum UserIdType {
		userName, mobile, email
	}

	protected void setUpDb(String userId, UserIdType type, String password) throws Exception {
		User user = new User("", "", "", encrypt(password)).setStatus(enabled).setId(UuidGeneratorUtil.generateUuid());
		user.getClass().getDeclaredMethod("set" + capitalize(type.toString()), String.class).invoke(user, userId);

		userRepository.save(user);

		Role role = new Role().setName("default").setId(UuidGeneratorUtil.generateUuid());
		App app = new App().setName("app_new").setDefaultRole(role).setId(UuidGeneratorUtil.generateUuid());
		UserAppRole userAppRole = new UserAppRole().setRole(role).setApp(app).setUser(user).setId(UuidGeneratorUtil.generateUuid());
		Permission permission = new Permission().setName("admin").setAllow(true).setId(UuidGeneratorUtil.generateUuid());
		permissionRepository.save(permission);

		roleRepository.save(role);
		appRepository.save(app);
		userAppRoleRepository.save(userAppRole);

		RolePermission rolePermission = new RolePermission().setRole(role).setPermission(permission).setId(UuidGeneratorUtil.generateUuid());
		rolePermissionRepository.save(rolePermission);
	}

	protected void clearDB() {
		rolePermissionRepository.deleteAll();
		userAppRoleRepository.deleteAll();
		appRepository.deleteAll();
		roleRepository.deleteAll();
		userRepository.deleteAll();
		permissionRepository.deleteAll();
	}

	@Test
	public void hello() throws Exception {
		assertEquals(1,1);
	}
}