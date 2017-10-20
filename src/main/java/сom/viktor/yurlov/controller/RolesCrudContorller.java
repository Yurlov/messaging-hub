package сom.viktor.yurlov.controller;

import gtedx.hylaa.iam.domain.*;
import om.viktor.yurlov.domain.*;
import сom.viktor.yurlov.domain.*;
import сom.viktor.yurlov.domain.rest.CreateRole;
import сom.viktor.yurlov.domain.rest.CreateRole.CreateRoleBody;
import сom.viktor.yurlov.domain.rest.GetUserRolesResponse;
import сom.viktor.yurlov.domain.rest.Response;
import gtedx.hylaa.iam.repository.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import om.viktor.yurlov.repository.*;
import сom.viktor.yurlov.repository.*;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import сom.viktor.yurlov.util.BeanUtil;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/roles")
public class RolesCrudContorller {
	RoleRepository roleRepository;
	UserAppRoleRepository userAppRoleRepository;
	RolePermissionRepository rolePermissionRepository;
	AppRepository appRepository;
	PermissionRepository permissionRepository;
	UserRepository userRepository;

	public RolesCrudContorller(RoleRepository roleRepository, UserAppRoleRepository userAppRoleRepository, RolePermissionRepository rolePermissionRepository, AppRepository appRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
		this.roleRepository = roleRepository;
		this.userAppRoleRepository = userAppRoleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.appRepository = appRepository;
		this.permissionRepository = permissionRepository;
		this.userRepository = userRepository;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Response> createRole(@RequestBody @Valid CreateRole createRoleRequest, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(createRoleRequest.getExtra(), null, emptyFieldErrors, 400);
		}

		CreateRoleBody role = createRoleRequest.getBody();

		if (roleRepository.findByName(role.getName()) != null) {
			return ControllerTools.makeResponse(createRoleRequest.getExtra(), null, "role with this name already exists", 400);
		}

		Role dbRole = new Role();
		BeanUtil.copyNotNullProperties(role, dbRole);

		Role parentRole = roleRepository.findByName(role.getParent());

		roleRepository.save(dbRole.setId(UuidGeneratorUtil.generateUuid()).setParent(parentRole));

		List<String> permissions = createRoleRequest.getBody().getPermissions();
		if (permissions != null && !permissions.isEmpty()) {
			Map<String, Permission> permissionMap = permissions.stream()
					.collect(Collectors.toMap(pName -> pName, permissionRepository::findByName));
			if (permissionMap.containsValue(null)) {
				String notFoundPermissions = permissionMap.entrySet().stream().filter(e -> e.getValue() == null)
						.map(Entry::getKey).collect(Collectors.joining(", "));
				return ControllerTools.makeResponse(createRoleRequest.getExtra(), null, "Permissions: " + notFoundPermissions + " not found ", 400);
			}

			List<RolePermission> rolePermissionList = permissionMap.values().stream()
					.map(permission -> new RolePermission().setId(UuidGeneratorUtil.generateUuid()).setPermission(permission).setRole(dbRole)).collect(Collectors.toList());

			rolePermissionRepository.save(rolePermissionList);
		}

		return ControllerTools.makeResponse(createRoleRequest.getExtra(), null, "success", 200);
	}

	@GetMapping
	public ResponseEntity<Response> getUserRoles(@RequestParam(value = "user", required = false) String userName,
												 @RequestParam(value = "app", required = false) String appName) {
		App app = appRepository.findByName(appName);
		if (app == null && appName != null) {
			return ControllerTools.makeResponse(null, null, "app not found", 404);
		}

		User user = userRepository.findByUserName(userName);
		if (user == null && userName != null) {
			return ControllerTools.makeResponse(null, null, "user not found", 404);
		}

		Collection<String> roles;
		if (user != null && app != null) {
			roles = userAppRoleRepository.findRolesByUserApp(user, app).stream().map(Role::getName).collect(Collectors.toList());
		} else if (user != null) {
			roles = userAppRoleRepository.findUserAppRoleByUser(user).stream().map(u -> u.getRole().getName()).collect(Collectors.toList());
		} else if (app != null) {
			roles = userAppRoleRepository.findUserAppRoleByApp(app).stream().map(u -> u.getRole().getName()).collect(Collectors.toList());
		} else {
			roles = roleRepository.findAll().stream().map(Role::getName).collect(Collectors.toList());
		}

		return ControllerTools.makeResponse(null, new GetUserRolesResponse()
						.setApp_name(appName)
						.setUsername(userName)
						.setRoles(roles),
				null, 200);
	}

	@GetMapping("/{roleName}")
	public ResponseEntity<Response> readRole(@PathVariable("roleName") String roleName) {
		Role role = roleRepository.findByName(roleName);
		return ControllerTools.makeResponse(null, role, role == null ? "not found" : "success", 200);
	}

	@PutMapping("/{roleName}")
	@Transactional
	public ResponseEntity<Response> updateRole(@PathVariable("roleName") String roleName, @RequestBody CreateRole roleRequest) {
		Role persistedRole = roleRepository.findByName(roleName);
		if (persistedRole == null) {
			return ControllerTools.makeResponse(roleRequest.getExtra(), null, "not found", 200);
		}

		CreateRoleBody roleBody = roleRequest.getBody();

		if (roleBody.getName() != null) {
			Role roleByName = roleRepository.findByName(roleBody.getName());
			if (roleByName != null && !roleByName.equals(persistedRole)) {
				return ControllerTools.makeResponse(roleRequest.getExtra(), null, "role with this name already exists", 400);
			}
		}

		BeanUtil.copyNotNullProperties(roleBody, persistedRole);

		if (roleBody.getParent() != null) {
			Role parent = roleRepository.findByName(roleBody.getParent());
			persistedRole.setParent(parent);
		}

		roleRepository.save(persistedRole);

		List<String> permissions = roleRequest.getBody().getPermissions();
		if (permissions != null && !permissions.isEmpty()) {
			Map<String, Permission> permissionMap = permissions.stream()
					.collect(Collectors.toMap(pName -> pName, permissionRepository::findByName));
			if (permissionMap.containsValue(null)) {
				String notFoundPermissions = permissionMap.entrySet().stream().filter(e -> e.getValue() == null)
						.map(Entry::getKey).collect(Collectors.joining(", "));
				return ControllerTools.makeResponse(roleRequest.getExtra(), null, "Permissions: " + notFoundPermissions + " not found ", 400);
			}

			rolePermissionRepository.delete(rolePermissionRepository.findRolePermissionByRole(persistedRole));

			List<RolePermission> rolePermissionList = permissionMap.values().stream()
					.map(permission -> new RolePermission().setId(UuidGeneratorUtil.generateUuid()).setPermission(permission).setRole(persistedRole)).collect(Collectors.toList());

			rolePermissionRepository.save(rolePermissionList);
		}

		return ControllerTools.makeResponse(roleRequest.getExtra(), null, "success", 200);
	}

	@DeleteMapping("/{roleName}")
	@Transactional
	public ResponseEntity<Response> deleteRole(@PathVariable("roleName") String roleName) {
		Role persistedRole = roleRepository.findByName(roleName);
		if (persistedRole == null) {
			return ControllerTools.makeResponse(null, null, "not found", 200);
		}

		userAppRoleRepository.delete(userAppRoleRepository.findUserAppRoleByRole(persistedRole));
		rolePermissionRepository.delete(rolePermissionRepository.findRolePermissionByRole(persistedRole));
		appRepository.delete(appRepository.findByDefaultRole(persistedRole));
		roleRepository.delete(persistedRole);

		return ControllerTools.makeResponse(null, null, "success", 200);
	}
}