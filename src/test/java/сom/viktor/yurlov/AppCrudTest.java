package сom.viktor.yurlov;

import сom.viktor.yurlov.controller.ControllerConstants;
import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.rest.CreateApp;
import сom.viktor.yurlov.domain.rest.CreateApp.CreateAppBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class AppCrudTest extends MvcTestCase {

	@Before
	public void init() throws Exception {
		initJhonSnow();
	}

	@Test
	public void postTest() throws Exception {
		CreateApp createApp = new CreateApp()
				.setBody(new CreateAppBody()
						.setName("superApp")
						.setDefault_role_name("default")
				);

		mvc.perform(post("/admin/apps")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createApp)))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		App app = appRepository.findByName("superApp");
		assertEquals(app.getName(), createApp.getBody().getName());
		assertEquals(app.getDefaultRole().getName(), createApp.getBody().getDefault_role_name());
	}

	@Test
	public void deleteTest() throws Exception {
		mvc.perform(delete("/admin/apps/app1"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));
	}

	@Test
	public void getTest() throws Exception {
		mvc.perform(get("/admin/apps/app1"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.body.name").value(app1.getName()))
				.andExpect(jsonPath("$.body.default_role_name").value(app1.getDefaultRole().getName()))
				.andExpect(jsonPath("$.body.id").value(app1.getId()))
				.andExpect(jsonPath("$.message").value("success"));
	}

	@Test
	public void updateTest() throws Exception {
		CreateApp createApp = new CreateApp()
				.setBody(new CreateAppBody()
						.setName("superApp")
						.setDefault_role_name("default")
				);

		mvc.perform(put("/admin/apps/app1")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createApp))
		)
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		App app = appRepository.findByName("superApp");
		assertEquals(app.getName(), createApp.getBody().getName());
		assertEquals(app.getDefaultRole().getName(), createApp.getBody().getDefault_role_name());
	}

	@Test
	public void getAppsTest() throws Exception {
		mvc.perform(get("/admin/apps")
				.param("user", "ghost")
				.param("role", "king")
				.header(ControllerConstants.HEADER_USER_ID, "ghost")
		)
				.andExpect(jsonPath("$.body.user").value("ghost"))
				.andExpect(jsonPath("$.body.role").value("king"))
				.andExpect(jsonPath("$.code").value(200))
		;
	}

	@After
	public void killStark() {
		clearDB();
	}
}