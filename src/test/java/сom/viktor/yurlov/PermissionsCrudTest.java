package сom.viktor.yurlov;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.RolePermission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import сom.viktor.yurlov.domain.rest.CreatePermission;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionsCrudTest extends MvcTestCase {
	Role mainRole = new Role().setName("MainRole").setDescription("AnyRole").setId("1");
	Role minorRole = new Role().setName("MinorRole").setDescription("do what they want").setId("2");
	Role player = new Role().setName("Player").setDescription("play cards").setId("3");

	Permission password = new Permission().setName("Password").setAllow(true).setResource("www.google.com").setId("1");
	Permission login = new Permission().setName("login").setAllow(false).setResource("www.google.com").setId("2");
	Permission guest = new Permission().setName("guest").setAllow(true).setResource("www.google.com").setId("3");

	@Before
	public void init() throws Exception {
		roleRepository.save(asList(mainRole, minorRole, player));
		permissionRepository.save(asList(password, login, guest));
		rolePermissionRepository.save(asList(
				new RolePermission().setPermission(password).setRole(mainRole).setId("1"),
				new RolePermission().setPermission(login).setRole(mainRole).setId("2"),
				new RolePermission().setPermission(guest).setRole(mainRole).setId("3"),
				new RolePermission().setPermission(login).setRole(minorRole).setId("4"),
				new RolePermission().setPermission(guest).setRole(minorRole).setId("5"),
				new RolePermission().setPermission(guest).setRole(player).setId("6")
				));
	}

	@Test
	public void postTest() throws Exception {
		CreatePermission createPermission = new CreatePermission().setBody(
				new CreatePermission.CreatePermissionBody()
				.setName("observer")
				.setAllow(true)
				.setAction("observe")
				.setResource("www.yahoo.com")
		);

		mvc.perform(post("/admin/permissions")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createPermission)))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		Permission permission = permissionRepository.findByName(createPermission.getBody().getName());
		assertEquals(createPermission.getBody().getAllow(), permission.getAllow());
		assertEquals(createPermission.getBody().getAction(), permission.getAction());
		assertEquals(createPermission.getBody().getResource(), permission.getResource());
	}

	@Test
	public void deleteTest() throws Exception {
		mvc.perform(delete("/admin/permissions/Password"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		assertNull(permissionRepository.findByName(password.getName()));
	}

	@Test
	public void getTest() throws Exception {
		mvc.perform(get("/admin/permissions/Password"))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.body.name").value(password.getName()))
				.andExpect(jsonPath("$.body.allow").value(password.getAllow()))
				.andExpect(jsonPath("$.body.id").value(password.getId()))
				.andExpect(jsonPath("$.body.resource").value(password.getResource()))
				.andExpect(jsonPath("$.body.action").value(password.getAction()))
		;
	}

	@Test
	public void updateTest() throws Exception {
		CreatePermission createPermission = new CreatePermission().setBody(
				new CreatePermission.CreatePermissionBody()
						.setName("observer")
						.setAllow(true)
						.setAction("observe")
						.setResource("www.yahoo.com")
		);

		mvc.perform(put("/admin/permissions/Password")
				.contentType(APPLICATION_JSON)
				.content(mapper.writeValueAsString(createPermission)))
				.andExpect(jsonPath("$.code").value(200))
				.andExpect(jsonPath("$.message").value("success"));

		Permission permission = permissionRepository.findByName(createPermission.getBody().getName());
		assertEquals(createPermission.getBody().getAllow(), permission.getAllow());
		assertEquals(createPermission.getBody().getAction(), permission.getAction());
		assertEquals(createPermission.getBody().getResource(), permission.getResource());

		assertNull(permissionRepository.findByName(password.getName()));
	}

	@After
	public void clear() {
		clearDB();
	}
}