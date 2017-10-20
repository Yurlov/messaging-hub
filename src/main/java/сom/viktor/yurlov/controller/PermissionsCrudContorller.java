package сom.viktor.yurlov.controller;

import сom.viktor.yurlov.domain.Permission;
import сom.viktor.yurlov.domain.rest.CreatePermission;
import сom.viktor.yurlov.domain.rest.Response;
import gtedx.hylaa.iam.repository.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import om.viktor.yurlov.repository.*;
import сom.viktor.yurlov.repository.*;
import сom.viktor.yurlov.util.BeanUtil;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;


@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/permissions")
public class PermissionsCrudContorller {
	RoleRepository roleRepository;
	UserAppRoleRepository userAppRoleRepository;
	RolePermissionRepository rolePermissionRepository;
	AppRepository appRepository;
	PermissionRepository permissionRepository;
	UserRepository userRepository;

	public PermissionsCrudContorller(RoleRepository roleRepository, UserAppRoleRepository userAppRoleRepository, RolePermissionRepository rolePermissionRepository, AppRepository appRepository, PermissionRepository permissionRepository, UserRepository userRepository) {
		this.roleRepository = roleRepository;
		this.userAppRoleRepository = userAppRoleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.appRepository = appRepository;
		this.permissionRepository = permissionRepository;
		this.userRepository = userRepository;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Response> createPermission(@RequestBody @Valid CreatePermission request, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		CreatePermission.CreatePermissionBody permission = request.getBody();
		if (permissionRepository.findByName(permission.getName()) != null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "permission with this name already exists", 400);
		}

		Permission dbPermission = new Permission();
		BeanUtil.copyNotNullProperties(permission, dbPermission);

		permissionRepository.save(dbPermission.setId(UuidGeneratorUtil.generateUuid()));

		return ControllerTools.makeResponse(request.getExtra(), null, "success", 200);
	}

	@GetMapping
	public ResponseEntity<Response> getPermissions() {
		return ControllerTools.makeResponse(null, permissionRepository.findAll(),
				null, 200);
	}

	@GetMapping("/{permissionName}")
	public ResponseEntity<Response> readApp(@PathVariable("permissionName") String permissionName) {
		Permission permission = permissionRepository.findByName(permissionName);
		if (permission == null) {
			ControllerTools.makeResponse(null, null, "not found", 200);
		}

		return ControllerTools.makeResponse(null, permission,
				"success", 200);
	}

	@PutMapping("/{permissionName}")
	@Transactional
	public ResponseEntity<Response> updateApp(@PathVariable("permissionName") String permissionName, @RequestBody CreatePermission request) {
		Permission persistedPermission = permissionRepository.findByName(permissionName);
		if (persistedPermission == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "not found", 403);
		}

		CreatePermission.CreatePermissionBody permissionBody = request.getBody();

		if (permissionBody.getName() != null) {
			Permission permissionByName = permissionRepository.findByName(permissionBody.getName());
			if (permissionByName != null && !permissionByName.equals(persistedPermission)) {
				return ControllerTools.makeResponse(request.getExtra(), null, "permission with this name already exists", 400);
			}
		}

		BeanUtil.copyNotNullProperties(permissionBody, persistedPermission);
		permissionRepository.save(persistedPermission);

		return ControllerTools.makeResponse(request.getExtra(), null, "success", 200);
	}

	@DeleteMapping("/{permissionName}")
	@Transactional
	public ResponseEntity<Response> deleteApp(@PathVariable("permissionName") String permissionName) {
		Permission persistedPermission = permissionRepository.findByName(permissionName);
		if (persistedPermission == null) {
			return ControllerTools.makeResponse(null, null, "not found", 403);
		}

		rolePermissionRepository.delete(rolePermissionRepository.findRolePermissionByPermission(persistedPermission));
		permissionRepository.delete(persistedPermission);

		return ControllerTools.makeResponse(null, null, "success", 200);
	}
}