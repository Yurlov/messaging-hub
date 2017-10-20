package сom.viktor.yurlov;


import junit.framework.TestCase;
import lombok.Getter;
import lombok.Setter;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.util.PasswordUtil;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.junit.After;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class MvcUpdateTest extends MvcTestCase {

	@Getter
	@Setter
	static final private class UpdateSecure {
		String old_password;
		String new_password;

		UpdateSecure(String oldPassword, String newPassword) {
			this.old_password = oldPassword;
			this.new_password = newPassword;
		}
	}

	@Test
	public void updatePasswordTest() throws Exception {
		String oldPass = "P@ssw0rd";
		String newPass = "NewPass@1";

		String userName = "Jacky";
		User user = new User(userName, "", "", PasswordUtil.encrypt(oldPass)).setStatus(User.UserStatus.enabled).setId(UuidGeneratorUtil.generateUuid());
		userRepository.save(user);

		mvc.perform(post("/update-password")
				.header(HEADER_USER_ID, userName)
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Request<>().setSecure(new UpdateSecure(oldPass, newPass)))))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("your password has been updated"));

		TestCase.assertTrue("Passwords not equal", PasswordUtil.compare(userRepository.findByUserName(userName).getPassword(), newPass));
	}

	@After
	public void clear() {
		clearDB();
	}
}