package сom.viktor.yurlov.controller;

import сom.viktor.yurlov.domain.App;
import сom.viktor.yurlov.domain.Role;
import сom.viktor.yurlov.domain.User;
import сom.viktor.yurlov.domain.UserAppRole;
import сom.viktor.yurlov.domain.rest.CreateUser;
import сom.viktor.yurlov.domain.rest.Response;
import сom.viktor.yurlov.repository.AppRepository;
import сom.viktor.yurlov.repository.RoleRepository;
import сom.viktor.yurlov.repository.UserAppRoleRepository;
import сom.viktor.yurlov.repository.UserRepository;
import сom.viktor.yurlov.util.BeanUtil;
import сom.viktor.yurlov.util.PasswordUtil;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/users")
public class UserCrudContorller {
	private final UserRepository userRepository;
	private final AppRepository appRepository;
	private final RoleRepository roleRepository;
	private final UserAppRoleRepository userAppRoleRepository;

	public UserCrudContorller(UserRepository userRepository, AppRepository appRepository,
							  RoleRepository roleRepository, UserAppRoleRepository userAppRoleRepository) {
		this.userRepository = userRepository;
		this.appRepository = appRepository;
		this.roleRepository = roleRepository;
		this.userAppRoleRepository = userAppRoleRepository;
	}

	@PostMapping
	public ResponseEntity<Response> createUser(@RequestBody @Valid CreateUser userRequest, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}
		CreateUser.CreateUserBody user = userRequest.getBody();
		if (user.getMobile() != null && user.getMobile().isEmpty()) user.setMobile(null);
		if (user.getEmail() != null && user.getEmail().isEmpty()) user.setEmail(null);

		if (userRepository.findByUserName(user.getUserName()) != null) {
			return ControllerTools.makeResponse(userRequest.getExtra(), null, "user with this username already exists", 400);
		}

		if (user.getEmail() == null && user.getMobile() == null) {
			return ControllerTools.makeResponse(userRequest.getExtra(), null, "you must provide either modile or email", 400);
		}

		if (user.getEmail() != null && !userRepository.findByEmail(user.getEmail()).isEmpty()) {
			return ControllerTools.makeResponse(userRequest.getExtra(), null, "user with this email already exists", 400);
		}

		if (user.getMobile() != null && !userRepository.findByMobile(user.getMobile()).isEmpty()) {
			return ControllerTools.makeResponse(userRequest.getExtra(), null, "user with this mobile already exists", 400);
		}

		User dbUser = new User();
		BeanUtil.copyNotNullProperties(user, dbUser);

		userRepository.save(dbUser
				.setId(UuidGeneratorUtil.generateUuid())
				.setPassword(user.getPassword() == null ? PasswordUtil.encrypt("P@ssw0rd") : PasswordUtil.encrypt(user.getPassword()))
				.setStatus(user.getStatus() == null ? User.UserStatus.enabled : user.getStatus())
		);

		return ControllerTools.makeResponse(userRequest.getExtra(), null, "success", 200);
	}

	@GetMapping
	public ResponseEntity<Response> readUsers(
			@RequestParam(value = "app", required = false) String appName,
			@RequestParam(value = "role", required = false) String roleName) {

		Role role = roleRepository.findByName(roleName);
		if (role == null && roleName != null) {
			return ControllerTools.makeResponse(null, null, "role not found", 404);
		}

		App app = appRepository.findByName(appName);
		if (app == null && appName != null) {
			return ControllerTools.makeResponse(null, null, "app not found", 404);
		}

		Collection<User> users;
		if (app != null && role != null) {
			users = userAppRoleRepository.findUsersByAppRole(app, role);
		} else if (app != null) {
			users = userAppRoleRepository.findUsersByApp(app);
		} else if (role != null) {
			users = userAppRoleRepository.findUsersByRole(role);
		} else {
			users = userRepository.findAll();
		}
		return ControllerTools.makeResponse(null, users, "success", 200);
	}

	@GetMapping("/{username}")
	public ResponseEntity<Response> readUser(@PathVariable("username") String userName) {
		User user = userRepository.findByUserName(userName);
		return ControllerTools.makeResponse(null, user, user == null ? "not found" : "success", 200);
	}

	@PutMapping("/{username}")
	public ResponseEntity<Response> updateUser(@PathVariable("username") String userName, @RequestBody CreateUser userRequest) {
		User persistedUser = userRepository.findByUserName(userName);
		if (persistedUser == null) {
			return ControllerTools.makeResponse(userRequest.getExtra(), null, "not found", 200);
		}

		CreateUser.CreateUserBody user = userRequest.getBody();
		if (user.getMobile() != null && user.getMobile().isEmpty()) user.setMobile(null);
		if (user.getEmail() != null && user.getEmail().isEmpty()) user.setEmail(null);

		if (user.getEmail() != null) {
			List<User> userByEmail = userRepository.findByEmail(user.getEmail());
			if (!userByEmail.isEmpty() && !userByEmail.contains(persistedUser)) {
				return ControllerTools.makeResponse(userRequest.getExtra(), null, "user with this email already exists", 400);
			}
		}

		if (user.getMobile() != null) {
			List<User> userByMobile = userRepository.findByMobile(user.getMobile());
			if (!userByMobile.isEmpty() && !userByMobile.contains(persistedUser)) {
				return ControllerTools.makeResponse(userRequest.getExtra(), null, "user with this mobile already exists", 400);
			}
		}

		BeanUtil.copyNotNullProperties(user, persistedUser);

		if (user.getPassword() != null) {
			persistedUser.setPassword(PasswordUtil.encrypt(user.getPassword()));
		}

		userRepository.save(persistedUser);

		return ControllerTools.makeResponse(userRequest.getExtra(), null, "success", 200);
	}

	@DeleteMapping("/{username}")
	public ResponseEntity<Response> deleteUser(@PathVariable("username") String userName) {
		User persistedUser = userRepository.findByUserName(userName);
		if (persistedUser == null) {
			return ControllerTools.makeResponse(null, null, "not found", 200);
		}

		Set<UserAppRole> userAppRoleByUser = userAppRoleRepository.findUserAppRoleByUser(persistedUser);
		userAppRoleRepository.delete(userAppRoleByUser);
		userRepository.delete(persistedUser.getId());
		return ControllerTools.makeResponse(null, null, "success", 200);
	}


}