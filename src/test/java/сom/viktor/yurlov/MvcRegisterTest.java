package сom.viktor.yurlov;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import сom.viktor.yurlov.domain.User;
import junit.framework.TestCase;
import сom.viktor.yurlov.util.PasswordUtil;
import org.junit.After;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.springframework.test.web.servlet.ResultActions;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class MvcRegisterTest extends MvcTestCase {
	Request<UserBody, Secure> registerRequest = new Request<UserBody, Secure>()
			.setBody(new UserBody()
					.setUsername("jhony")
					.setMobile("098123231123")
					.setFirst_name("Jho")
					.setLast_name("Me")
					.setAddress("Kyiv")
					.setEmail("some@where.com"))
			.setSecure(new Secure().setPassword("P@ssw0rd"));

	@Test
	public void registerTest() throws Exception {
		ResultActions actions = sendRegister(registerRequest)
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"))
				.andExpect(status().is(200));

		Stream.of(UserBody.class.getDeclaredFields())
				.forEach(f -> {
					try {
						if (f.get(registerRequest.getBody()) != null) {
							actions.andExpect(jsonPath("$.body." + f.getName()).value(f.get(registerRequest.getBody()).toString()));
						}
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});

		User savedUser = userRepository.findByUserName(registerRequest.getBody().getUsername());
		actions.andExpect(jsonPath("$.body.id").value(savedUser.getId()));
		TestCase.assertTrue("Passwords are not equal", PasswordUtil.compare(savedUser.getPassword(), registerRequest.getSecure().getPassword()));
	}

	@Test
	public void registerFailByUserNameTest() throws Exception {
		sendRegister(registerRequest)
			.andExpect(status().is(200));

		sendRegister(new Request<UserBody, Secure>()
				.setBody(new UserBody()
						.setUsername("jhony")
						.setMobile("098123231111")
						.setFirst_name("Jho")
						.setLast_name("Me")
						.setAddress("Kyiv")
						.setEmail("any@where.com"))
				.setSecure(new Secure().setPassword("P@ssw0rd")))
				.andExpect(jsonPath("$.code").value(400))
				.andExpect(jsonPath("$.message").value("user with this username already exists"))
				.andExpect(status().is(400));
	}

	@Test
	public void registerFailByPhoneTest() throws Exception {
		sendRegister(registerRequest)
			.andExpect(status().is(200));

		sendRegister(new Request<UserBody, Secure>()
				.setBody(new UserBody()
						.setUsername("boob")
						.setMobile("098123231123")
						.setFirst_name("Jho")
						.setLast_name("Me")
						.setAddress("Kyiv")
						.setEmail("any@where.com"))
				.setSecure(new Secure().setPassword("P@ssw0rd")))
				.andExpect(jsonPath("$.code").value(400))
				.andExpect(jsonPath("$.message").value("user with this mobile already exists"))
				.andExpect(status().is(400));
	}

	@Test
	public void registerFailByMailTest() throws Exception {
		sendRegister(registerRequest)
			.andExpect(status().is(200));

		sendRegister(new Request<UserBody, Secure>()
				.setBody(new UserBody()
						.setUsername("boob")
						.setMobile("098123231122")
						.setFirst_name("Jho")
						.setLast_name("Me")
						.setAddress("Kyiv")
						.setEmail("some@where.com"))
				.setSecure(new Secure().setPassword("P@ssw0rd")))
				.andExpect(jsonPath("$.code").value(400))
				.andExpect(jsonPath("$.message").value("user with this email already exists"))
				.andExpect(status().is(400));
	}

	@Test
	public void activateTest() throws Exception {
		List smsRequest = new ArrayList();

		BDDMockito.when(template.postForEntity(anyString(), anyObject(), anyObject())).thenAnswer(invocationOnMock -> {
			smsRequest.add(invocationOnMock.getArguments()[1]);
			return null;
		});

		sendRegister(registerRequest)
			.andExpect(status().is(200));

		assertTrue("sms didn't send", !smsRequest.isEmpty() && smsRequest.get(0) instanceof SendMessageRequest);

		String url = ((SendMessageRequest) smsRequest.get(0)).getBody().getMessage();

		String code = url.split("code=")[1];

		mvc.perform(post("/activate").param("code", code))
				.andExpect(jsonPath("$.message").value("your account jhony is activated"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(status().is(200));
	}

	public ResultActions sendRegister(Request request) throws Exception {
		String jsonContent = mapper.writeValueAsString(request);

		return mvc.perform(post("/register")
				.contentType(APPLICATION_JSON)
				.content(jsonContent));
	}

	@After
	public void clear() {
		clearDB();
	}
}