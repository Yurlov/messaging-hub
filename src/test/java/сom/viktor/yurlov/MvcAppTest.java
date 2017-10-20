package сom.viktor.yurlov;

import lombok.Getter;
import lombok.Setter;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.util.PasswordUtil;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.junit.After;
import org.junit.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class MvcAppTest extends MvcTestCase {

	@Setter
	@Getter
	private static class AppBody {
		String app_name;

		public AppBody(String name) {
			this.app_name = name;
		}
	}

	@Test
	public void postJoinAppTest() throws Exception {
		String userName = "Join";
		User user = new User(userName, "", "", PasswordUtil.encrypt("P@ssw0rd")).setStatus(User.UserStatus.enabled).setId(UuidGeneratorUtil.generateUuid());
		userRepository.save(user);

		String appName = "new_app";
		Role role = new Role().setName("default").setId(UuidGeneratorUtil.generateUuid());
		App app = new App().setName(appName).setDefaultRole(role).setId(UuidGeneratorUtil.generateUuid());

		roleRepository.save(role);
		appRepository.save(app);

		mvc.perform(post("/join-app")
				.header(HEADER_USER_ID, userName)
				.contentType(APPLICATION_JSON).content(mapper.writeValueAsString(new Request<>().setBody(new AppBody(appName)))))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("OK"));
	}

	@After
	public void clear() {
		clearDB();
	}
}