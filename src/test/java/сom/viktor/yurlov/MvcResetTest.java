package сom.viktor.yurlov;


import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.domain.rest.SendMessageRequest;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.junit.After;
import org.junit.Test;
import org.mockito.BDDMockito;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class MvcResetTest extends MvcTestCase {

	@Getter
	@Setter
	private static class NewSecure {
		String new_password;

		public NewSecure(String password) {
			this.new_password = password;
		}
	}

	@Test
	public void postReset() throws Exception {
		String userId = "user";
		Request resetRequest = new Request<>().setBody(new ResetBody().setUser_id(userId).setBy_link(true));

		User user = new User(userId, "", "", "password").setEmail("gtedx@mailinator.com")
				.setStatus(User.UserStatus.enabled).setId(UuidGeneratorUtil.generateUuid());
		userRepository.save(user);

		List msgRequest = new ArrayList();

		BDDMockito.when(template.postForEntity(anyString(), anyObject(), anyObject())).thenAnswer(invocationOnMock -> {
			msgRequest.add(invocationOnMock.getArguments()[1]);
			return null;
		});

		mvc.perform(post("/reset-password-request")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(resetRequest)))
				.andExpect(jsonPath("$.code").value(200));

		assertTrue("email didn't send", !msgRequest.isEmpty() && msgRequest.get(0) instanceof SendMessageRequest);

		String url = ((SendMessageRequest)msgRequest.get(0)).getBody().getMessage();

		String code = url.split("code=")[1];

		mvc.perform(post("/reset-password")
				.param("code", code)
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(new Request<>().setSecure(new NewSecure("P@ssw0rd123")))))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath(".message").value("your password has been reset"));
	}

	@Test
	public void postResetFail() throws Exception {
		String userId = "user";
		Request resetRequest = new Request<>().setBody(new LoginBody().setUser_id(userId));

		mvc.perform(post("/reset-password-request")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(resetRequest)))
				.andExpect(jsonPath("$.code").value(404));
	}

	@After
	public void clear() {
		clearDB();
	}
}
