package сom.viktor.yurlov;

import org.junit.After;
import org.junit.Test;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MvcLoginTest extends MvcTestCase {

	@Test
	public void loginTestViaUserName() throws Exception {
		assertLogin("superBoss", UserIdType.userName);
	}

	@Test
	public void loginTestViaMobile() throws Exception {
		assertLogin("0984034811", UserIdType.mobile);
	}

	@Test
	public void loginTestViaMail() throws Exception {
		assertLogin("some@where.com", UserIdType.email);
	}

	private void assertLogin(String userId, UserIdType type) throws Exception {
		String password = "P@ssw0rd";

		Request<LoginBody, Secure> request = new Request<LoginBody, Secure>()
				.setBody(new LoginBody().setUser_id(userId))
				.setSecure(new Secure().setPassword(password));

		setUpDb(userId, type, password);

		mvc.perform(post("/login")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(jsonPath("$.body").exists())
				.andExpect(jsonPath("$.body." + type.toString().toLowerCase()).value(userId))
				.andExpect(jsonPath("$.body.apps").exists())
				.andExpect(jsonPath("$.body.apps[0].app").value("app_new"))
				.andExpect(jsonPath("$.body.apps[0].roles[0].name").value("default"))
				.andExpect(jsonPath("$.body.apps[0].roles[0].permissions[0].name").value("admin"));
	}

	@After
	public void clear() {
		clearDB();
	}
}