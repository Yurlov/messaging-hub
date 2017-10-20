package сom.viktor.yurlov;

import сom.viktor.yurlov.controller.ControllerConstants;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.rest.CreateRole;
import сom.viktor.yurlov.domain.rest.CreateRole.CreateRoleBody;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class RoleCrudTest extends MvcTestCase {

	@Before
	public void init() throws Exception {
		initJhonSnow();
	}

	@Test
	public void postTest() throws Exception {
		CreateRole createRole = new CreateRole()
				.setBody(new CreateRoleBody()
						.setName("prince")
						.setDescription("power overwhelming")
						.setParent("king")
						.setPermissions(asList(permission1.getName(), permission2.getName(), permission3.getName()))
				);

		mvc.perform(post("/admin/roles")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createRole)))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		Role prince = roleRepository.findByName("prince");
		assertEquals(prince.getName(), createRole.getBody().getName());
		assertEquals(prince.getDescription(), createRole.getBody().getDescription());
		assertEquals(prince.getParent().getName(), createRole.getBody().getParent());
	}

	@Test
	public void deleteTest() throws Exception {
		mvc.perform(delete("/admin/roles/king"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));
	}

	@Test
	public void getTest() throws Exception {
		mvc.perform(get("/admin/roles/king"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.body.name").value(kingRole.getName()))
				.andExpect(jsonPath("$.body.description").value(kingRole.getDescription()))
				.andExpect(jsonPath("$.body.id").value(kingRole.getId()))
				.andExpect(jsonPath("$.body.parent").value(kingRole.getParent()))
				.andExpect(jsonPath("$.message").value("success"));
	}

	@Test
	public void updateTest() throws Exception {
		CreateRole createRole = new CreateRole()
				.setBody(new CreateRoleBody()
						.setDescription("power overwhelming")
						.setParent("default").setPermissions(asList(permission1.getName())));

		mvc.perform(put("/admin/roles/king")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createRole))
		)
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		Role king = roleRepository.findByName("king");
		assertEquals(king.getName(), "king");
		assertEquals(king.getDescription(), createRole.getBody().getDescription());
		assertEquals(king.getParent().getName(), createRole.getBody().getParent());
	}

	@Test
	public void getAllRolesTest() throws Exception {
		mvc.perform(get("/admin/roles")
				.param("user", "ghost")
				.param("app", "app1")
				.header(ControllerConstants.HEADER_USER_ID, "ghost")
		)
				.andExpect(jsonPath("$.body.app_name").value("app1"))
				.andExpect(jsonPath("$.code").value(200))
		;
	}

	@After
	public void killStark() {
		clearDB();
	}
}