package сom.viktor.yurlov.controller;

import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.domain.rest.CreateApp;
import сom.viktor.yurlov.domain.rest.GetAppsResponse;
import сom.viktor.yurlov.domain.rest.Response;
import сom.viktor.yurlov.repository.AppRepository;
import сom.viktor.yurlov.repository.RoleRepository;
import сom.viktor.yurlov.repository.UserAppRoleRepository;
import сom.viktor.yurlov.repository.UserRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import сom.viktor.yurlov.util.UuidGeneratorUtil;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Collection;
import java.util.stream.Collectors;

import static сom.viktor.yurlov.controller.ControllerTools.checkForEmptyFields;
import static сom.viktor.yurlov.controller.ControllerTools.makeResponse;
import static сom.viktor.yurlov.util.BeanUtil.copyNotNullProperties;


@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/apps")
public class AppsCrudContorller {
	RoleRepository roleRepository;
	UserAppRoleRepository userAppRoleRepository;
	AppRepository appRepository;
	UserRepository userRepository;

	public AppsCrudContorller(RoleRepository roleRepository, UserAppRoleRepository userAppRoleRepository, AppRepository appRepository, UserRepository userRepository) {
		this.roleRepository = roleRepository;
		this.userAppRoleRepository = userAppRoleRepository;
		this.appRepository = appRepository;
		this.userRepository = userRepository;
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Response> createApp(@RequestBody @Valid CreateApp request, BindingResult bindingResult) {
		String emptyFieldErrors = checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		CreateApp.CreateAppBody app = request.getBody();
		if (appRepository.findByName(app.getName()) != null) {
			return makeResponse(request.getExtra(), null, "app with this name already exists", 400);
		}

		App dbApp = new App();
		copyNotNullProperties(app, dbApp);

		if (app.getDefault_role_name() != null) {
			Role defaultRole = roleRepository.findByName(app.getDefault_role_name());
			if (defaultRole == null) return makeResponse(request.getExtra(), null, "role not found", 400);
			dbApp.setDefaultRole(defaultRole);
		}

		appRepository.save(dbApp.setId(UuidGeneratorUtil.generateUuid()));

		return makeResponse(request.getExtra(), null, "success", 200);
	}

	@GetMapping
	public ResponseEntity<Response> getApps(@RequestParam(value = "user", required = false) String userName,
											@RequestParam(value = "role", required = false) String roleName) {
		Role role = roleRepository.findByName(roleName);
		if (role == null && roleName != null) {
			return makeResponse(null, null, "role not found", 404);
		}

		User user = userRepository.findByUserName(userName);
		if (user == null && userName != null) {
			return makeResponse(null, null, "user not found", 404);
		}

		Collection<String> apps;

		if (user != null && role != null) {
			apps = userAppRoleRepository.findAppByRoleAndUser(role, user).stream()
					.map(a -> a.getApp().getName()).collect(Collectors.toList());
		} else if (user != null) {
			apps = userAppRoleRepository.findAppsByUser(user).stream()
					.map(App::getName).collect(Collectors.toList());
		} else if (role != null) {
			apps = userAppRoleRepository.findUserAppRoleByRole(role).stream()
					.map(a -> a.getApp().getName()).collect(Collectors.toList());
		} else {
			apps = appRepository.findAll().stream()
					.map(App::getName).collect(Collectors.toList());
		}

		return makeResponse(null, new GetAppsResponse()
						.setRole(roleName)
						.setUser(userName)
						.setApps(apps),
				null, 200);
	}

	@GetMapping("/{appName}")
	public ResponseEntity<Response> readApp(@PathVariable("appName") String appName) {
		App app = appRepository.findByName(appName);
		if (app == null) {
			makeResponse(null, null, "not found", 200);
		}

		return makeResponse(null, new CreateApp.CreateAppBody()
				.setId(app.getId())
				.setDefault_role_name(app.getDefaultRole().getName())
				.setName(app.getName()), "success", 200);
	}

	@PutMapping("/{appName}")
	@Transactional
	public ResponseEntity<Response> updateApp(@PathVariable("appName") String appName, @RequestBody CreateApp request) {
		App persistedApp = appRepository.findByName(appName);
		if (persistedApp == null) {
			return makeResponse(request.getExtra(), null, "not found", 403);
		}

		CreateApp.CreateAppBody appBody = request.getBody();

		if (appBody.getName() != null) {
			App appByName = appRepository.findByName(appBody.getName());
			if (appByName != null && !appByName.equals(persistedApp)) {
				return makeResponse(request.getExtra(), null, "app with this name already exists", 400);
			}
		}

		copyNotNullProperties(appBody, persistedApp);

		if (appBody.getDefault_role_name() != null) {
			Role defaultRole = roleRepository.findByName(appBody.getDefault_role_name());
			persistedApp.setDefaultRole(defaultRole);
		}

		appRepository.save(persistedApp);

		return makeResponse(request.getExtra(), null, "success", 200);
	}

	@DeleteMapping("/{appName}")
	@Transactional
	public ResponseEntity<Response> deleteApp(@PathVariable("appName") String appName) {
		App persistedApp = appRepository.findByName(appName);
		if (persistedApp == null) {
			return makeResponse(null, null, "not found", 403);
		}

		userAppRoleRepository.delete(userAppRoleRepository.findUserAppRoleByApp(persistedApp));
		appRepository.delete(persistedApp);

		return makeResponse(null, null, "success", 200);
	}
}