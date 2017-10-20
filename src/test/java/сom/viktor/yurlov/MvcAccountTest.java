package сom.viktor.yurlov;


import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.junit.After;
import org.junit.Test;

import static сom.viktor.yurlov.domain.User.UserStatus.enabled;
import static сom.viktor.yurlov.util.PasswordUtil.encrypt;
import static junit.framework.TestCase.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class MvcAccountTest extends MvcTestCase {

	@Test
	public void getMyAccountTest() throws Exception {
		String userName = "Jacky";

		setUpDb(userName, UserIdType.userName, "P@ssw0rd");

		mvc.perform(get("/my-account").header(HEADER_USER_ID, userName))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("OK"))
				.andExpect(jsonPath("$.body.username").value(userName))
				.andExpect(jsonPath("$.body.apps").exists())
				.andExpect(jsonPath("$.body.apps[0].app").value("app_new"))
				.andExpect(jsonPath("$.body.apps[0].roles[0].name").value("default"))
				.andExpect(jsonPath("$.body.apps[0].roles[0].permissions[0].name").value("admin"));
	}

	@Test
	public void postMyAccountTest() throws Exception {
		String newAddress = "newAddress";
		Request request = new Request<>().setBody(new UserBody().setAddress(newAddress));

		String userName = "Jacky";
		User user = new User(userName, "", "", encrypt("pass"))
				.setStatus(enabled)
				.setAddress("oldAddress")
				.setId(UuidGeneratorUtil.generateUuid());

		userRepository.save(user);

		mvc.perform(post("/my-account")
				.header(HEADER_USER_ID, userName)
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(request)))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		assertEquals("Address wasn't updated", userRepository.findByUserId(userName).getAddress(), newAddress);
	}

	@After
	public void clear() {
		clearDB();
	}
}