package сom.viktor.yurlov.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import gtedx.hylaa.iam.domain.*;
import om.viktor.yurlov.domain.*;
import gtedx.hylaa.iam.domain.rest.*;
import сom.viktor.yurlov.domain.*;
import сom.viktor.yurlov.domain.rest.*;
import сom.viktor.yurlov.repository.AppRepository;
import сom.viktor.yurlov.repository.RoleRepository;
import сom.viktor.yurlov.repository.UserAppRoleRepository;
import сom.viktor.yurlov.repository.UserRepository;
import сom.viktor.yurlov.services.UserPermissionService;
import сom.viktor.yurlov.util.PasswordUtil;
import сom.viktor.yurlov.util.PasswordValidator;
import om.viktor.yurlov.domain.rest.*;
import сom.viktor.yurlov.util.UuidGeneratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static org.thymeleaf.util.StringUtils.isEmptyOrWhitespace;


@RestController
public class UserController {

	private final String secretKey;
	private final String hostUrl;
	private final String messagingHubUrl;

	private final UserRepository userRepository;
	private final AppRepository appRepository;
	private final UserAppRoleRepository userAppRoleRepository;
	private final RoleRepository roleRepository;

	private final UserPermissionService userPermissionService;
	private final RestTemplate template;
	private final PasswordValidator passwordValidator;


    @Autowired
	private final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
	private SetOperations<String, String> verificationCodes = redisTemplate.opsForSet();
	private SetOperations<String, String> resetPasswordCodes = redisTemplate.opsForSet();
	private HashOperations<String,Integer,Long> loginHistory = redisTemplate.opsForHash();


	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final Random random = new Random();


    public UserController(@Value("${iam.secret-key}") String secretKey,
                          @Value("${iam.address}") String hostUrl,
                          @Value("${messaging-hub.address}") String messagingHubUrl,
                          @Value("${iam.password.min-total-characters}") int minTotalCharacters,
                          @Value("${iam.password.min-upper-characters}") int minUpperCharacters,
                          @Value("${iam.password.min-lower-characters}") int minLoverCharacters,
                          @Value("${iam.password.min-special-characters}") int minSpecialCharacters,
                          @Value("${iam.password.min-digits}") int minDigits,
                          UserRepository userRepository,
                          AppRepository appRepository,
                          UserAppRoleRepository userAppRoleRepository,
                          RoleRepository roleRepository, UserPermissionService userPermissionService,
                          RestTemplate template) {
		this.passwordValidator = new PasswordValidator(minTotalCharacters, minUpperCharacters, minLoverCharacters, minSpecialCharacters, minDigits);
		this.secretKey = secretKey;
		this.hostUrl = hostUrl;
		this.messagingHubUrl = messagingHubUrl;
		this.userRepository = userRepository;
		this.appRepository = appRepository;
		this.userAppRoleRepository = userAppRoleRepository;
		this.roleRepository = roleRepository;
		this.userPermissionService = userPermissionService;
		this.template = template;
    }

	@PostMapping("/verify")
	public ResponseEntity<Response> verify(@RequestBody @Valid VerifyRequest request,
										   BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {

			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);

		}
		String code = generateCode();
		String receiver = request.getBody().getReceiver();

		verificationCodes.add(receiver,code);

		sendVerificationCode(receiver, code);

		return ControllerTools.makeResponse(request.getExtra(), null, "Code has been sent to " + receiver, 200);


	}

	private void sendVerificationCode(String receiver, String code) {
		String message = "Your verification code is " + code;
		if (receiver.startsWith("+86")) {
			message = "{\"type\": \"register\", \"code\": \"" + code + "\"}";
		}
		sendMessage(receiver, message);
	}

	@PostMapping("/register")
	public ResponseEntity<Response> register(@RequestBody @Valid RegisterRequest request, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		User user = request.getUser();
		if (userRepository.findByUserName(user.getUserName()) != null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user with this username already exists", 400);
		}

		if (isEmptyOrWhitespace(user.getEmail())) {
			user.setEmail(null);
		}
		if (isEmptyOrWhitespace(user.getMobile())) {
			user.setMobile(null);
		}

		if (user.getEmail() == null && user.getMobile() == null) {
			return makeResponseWithExtra(request.getExtra(), null, "you must provide either mobile or email", 400);
		}

		if (user.getEmail() != null && !userRepository.findByEmail(user.getEmail()).isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user with this email already exists", 400);
		}

		if (user.getMobile() != null && !userRepository.findByMobile(user.getMobile()).isEmpty()) {

			return ControllerTools.makeResponse(request.getExtra(), null, "user with this mobile already exists", 400);


		}

		if (request.getBody().getVerificationCode() != null) {
			String receiver = user.getEmail() != null ? user.getEmail() : user.getMobile();
			String realCode = verificationCodes.get(receiver);

			if (realCode == null || !realCode.equals(request.getBody().getVerificationCode())) {
				return ControllerTools.makeResponse(request.getExtra(), null, "Wrong verification code", 400);
			}
			verificationCodes.remove(user.getMobile());

		}

		String result = passwordValidator.validate(request.getSecure().getPassword());
		if (!"".equals(result)) {
			return ControllerTools.makeResponse(request.getExtra(), null, result, 400);
		}

		user.setPassword(PasswordUtil.encrypt(request.getSecure().getPassword())).setId(UuidGeneratorUtil.generateUuid());

		if (request.getBody().getVerificationCode() != null) {
			user.setStatus(User.UserStatus.enabled);
		} else {
			user.setStatus(User.UserStatus.pending);
			String receiver = user.getEmail() != null ? user.getEmail() : user.getMobile();
			String activationUrl = hostUrl + "/ui/activate?code=" + new Code(Code.ACTIVATION, receiver).encrypt(secretKey);
			String messageText = "please activate within 24 hours\n" + activationUrl;
			sendMessage(receiver, messageText);
		}

		User saved = userRepository.save(user);

		return ControllerTools.makeResponse(request.getExtra(), saved, "success", 200);

	}

	///-------------------------------------------------------------
	@PostMapping("/activate")
	public ResponseEntity<Response> activate(@RequestParam String code) {
		Code decrypted = Code.decrypt(code, secretKey);

		if (decrypted == null || !decrypted.type().equals(Code.ACTIVATION)) {
			return ControllerTools.makeResponse(null, null, "invalid code", 400);
		}

		if (System.currentTimeMillis() - decrypted.timestamp() > Duration.ofDays(1).toMillis()) {
			return ControllerTools.makeResponse(null, null, "expired code", 400);
		}

		User user = userRepository.findByUserId(decrypted.userId());
		if (user == null) {
			return ControllerTools.makeResponse(null, null, "user not found", 404);
		}
		user.setStatus(User.UserStatus.enabled);
		userRepository.save(user);

		return ControllerTools.makeResponse(null, null, String.format("your account %s is activated", user.getUserName()), 200);
	}

	@PostMapping("/send-activation-link")
	public ResponseEntity<Response> sendActivationLink(@RequestBody @Valid ActivationLinkRequest request, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		String userId = request.getBody().getUserId();

		User user = userRepository.findByUserId(userId);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "User id doesn't exist", 400);
		}

		String activationUrl = hostUrl + "/activate?code=" + new Code(Code.ACTIVATION, user.getUserName()).encrypt(secretKey);
		String messageText = "please activate within 24 hours\n" + activationUrl;
		String receiver = user.getEmail() != null ? user.getEmail() : user.getMobile();
		sendMessage(receiver, messageText);

		return ControllerTools.makeResponse(request.getExtra(), null, "Activation link is successfully sent to " + receiver, 200);

	}

	@PostMapping("/login")
	public ResponseEntity<Response> login(@RequestBody @Valid LoginRequest request, BindingResult bindingResult) {

		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		String userId = request.getBody().getUserId();
		String password = request.getSecure().getPassword();

		if (!canLogin(userId)) {
			return ControllerTools.makeResponse(request.getExtra(), null, "please try again after " + getThrottling(userId) + " seconds", 403);
		}

		User user = userRepository.findByUserId(userId);
		if (user == null) {
			loginFailed(userId);
			return ControllerTools.makeResponse(request.getExtra(), null, ControllerConstants.AUTHENTICATION_FAILED, 401);
		}

		Long userCounter;
		if (!PasswordUtil.compare(user.getPassword(), password)) {
			userCounter = counter.incrementAndGet();
			if(userCounter>10){

			}

			loginFailed(userId);
			return ControllerTools.makeResponse(request.getExtra(), null, ControllerConstants.AUTHENTICATION_FAILED, 401);
		}

		if (user.getStatus() == User.UserStatus.pending) {
			String activationUrl = hostUrl + "/activate?code=" + new Code(Code.ACTIVATION, user.getUserName()).encrypt(secretKey);
			String messageText = "Please activate your account within 24 hours\n" + activationUrl;
			String receiver = user.getEmail() != null ? user.getEmail() : user.getMobile();
			sendMessage(receiver, messageText);

			return ControllerTools.makeResponse(request.getExtra(), null, "activate your account first", 401);
		}

		allowLogin(user);

		Map<App, Map<Role, List<Permission>>> userPermissions = userPermissionService.getUserPermissions(user);

		return ControllerTools.makeResponse(request.getExtra(), new UserResponse(user, userPermissions), "success", 200);
	}

	@PostMapping("/reset-password-request")
	public ResponseEntity<Response> resetPasswordRequest(@RequestBody @Valid ResetPasswordRequest request, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		String userId = request.getBody().getUserId();
		User user = userRepository.findByUserId(userId);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		String message;
		String receiver = user.getEmail() != null ? user.getEmail() : user.getMobile();
		if (request.getBody().isByLink()){
			sendPasswordResetLink(receiver, request.getBody().getUrl());
			message = "Reset link has been sent to " + receiver + ".";
		} else {
			String code = generateCode();
            resetPasswordCodes.add(receiver,code);
			sendPasswordResetCode(receiver, code);
			message = "Code has been sent to " + receiver +
					". Please go to " + hostUrl + "/ui/reset-password" + " to reset your password.";
		}

		return ControllerTools.makeResponse(request.getExtra(), null, message, 200);
	}

	private void sendPasswordResetLink(String receiver, String resetUrl) {
		String resetFormUrl = resetUrl != null ? resetUrl : hostUrl + "/ui/reset-password";
		String delimeter = resetFormUrl.contains("?") ? "&" : "?";
		String resetPasswordUrl = resetFormUrl + delimeter + "code=" + new Code(Code.PASSWORD, receiver).encrypt(secretKey);
		sendMessage(receiver, "Please set you password using the link below. "
				+ "This link is valid for only 30 minutes.\n" + resetPasswordUrl);
	}

	private void sendPasswordResetCode(String receiver, String code) {
		String message = "Code to reset your password: " + code;
		if(receiver.startsWith("+86")) {
			message = "{\"type\": \"reset_password\", \"code\": \"" + code + "\"}";
		}
		sendMessage(receiver, message);
	}

	@PostMapping(value = "/reset-password")
	public ResponseEntity<Response> resetPasswordByCode(@RequestBody @Valid ResetPasswordConfirmRequest request,
														  BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		String receiver = request.getBody().getReceiver();
		String realCode = resetPasswordCodes.randomMember(receiver);

		if (realCode == null || !realCode.equals(request.getBody().getCode())) {
			return ControllerTools.makeResponse(request.getExtra(), null, "Invalid code", 400);
		}

		String newPassword = request.getSecure().getNewPassword();
		String result = passwordValidator.validate(request.getSecure().getNewPassword());
		if (!"".equals(result)) {
			return ControllerTools.makeResponse(request.getExtra(), null, result, 400);
		}

		User user = userRepository.findByUserId(receiver);
		user.setPassword(PasswordUtil.encrypt(newPassword));
		userRepository.save(user);
		resetPasswordCodes.remove(receiver);

		allowLogin(user);

		return ControllerTools.makeResponse(request.getExtra(), new UserNameResponse(user.getUserName()), "your password has been reset", 200);
	}

	@PostMapping(value = "/reset-password", params = "code")
	public ResponseEntity<Response> resetPasswordByLink(@RequestBody @Valid ResetPasswordConfirmRequest request,
								  @RequestParam String code, BindingResult bindingResult) {
		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		Code decrypted = Code.decrypt(code, secretKey);
		String newPassword = request.getSecure().getNewPassword();

		if (decrypted == null || !decrypted.type().equals(Code.PASSWORD)) {
			return ControllerTools.makeResponse(request.getExtra(), null, "invalid code", 400);
		}

		if (System.currentTimeMillis() - decrypted.timestamp() > Duration.ofMinutes(30).toMillis()) {
			return ControllerTools.makeResponse(request.getExtra(), null, "expired code", 400);
		}

		User user = userRepository.findByUserId(decrypted.userId());
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		String result = passwordValidator.validate(request.getSecure().getNewPassword());
		if (!"".equals(result)) {
			return ControllerTools.makeResponse(request.getExtra(), null, result, 400);
		}

		user.setPassword(PasswordUtil.encrypt(newPassword));
		userRepository.save(user);

		allowLogin(user);

		return ControllerTools.makeResponse(request.getExtra(), new UserNameResponse(user.getUserName()), "your password has been reset", 200);
	}

	@PostMapping("/update-password")
	public ResponseEntity<Response> updatePassword(@RequestHeader(ControllerConstants.HEADER_USER_ID) String userName,
								   @RequestBody UpdatePasswordRequest request) {

		User user = userRepository.findByUserName(userName);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		String oldPassword = request.getSecure().getOldPassword();
		String newPassword = request.getSecure().getNewPassword();

		if (!PasswordUtil.compare(user.getPassword(), oldPassword)) {
			return ControllerTools.makeResponse(request.getExtra(), null, "wrong password", 405);
		}

		String result = passwordValidator.validate(request.getSecure().getNewPassword());
		if (!"".equals(result)) {
			return ControllerTools.makeResponse(request.getExtra(), null, result, 400);
		}

		user.setPassword(PasswordUtil.encrypt(newPassword));
		userRepository.save(user);

		return ControllerTools.makeResponse(request.getExtra(), new UserNameResponse(user.getUserName()), "your password has been updated", 200);
	}

	@GetMapping("/my-account")
	public ResponseEntity<Response> getMyAccount(@RequestHeader(ControllerConstants.HEADER_USER_ID) String userName) {

		User user = userRepository.findByUserName(userName);
		if (user == null) {
			return new ResponseEntity<>(new Response().setCode(404).setMessage("user not found"), HttpStatus.NOT_FOUND);
		}

		Map<App, Map<Role, List<Permission>>> userPermissions = userPermissionService.getUserPermissions(user);

		return new ResponseEntity<>(new Response().setBody(new UserResponse(user, userPermissions)).setCode(200).setMessage("OK"), HttpStatus.OK);
	}

	@PostMapping("/my-account")
	public ResponseEntity<Response> updateMyAccount(@RequestHeader(ControllerConstants.HEADER_USER_ID) String userName,
									@RequestBody UpdateAccountRequest request) {

		User user = userRepository.findByUserName(userName);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		User updated = request.getBody();
		updated.setId(user.getId());
		updated.setUserName(user.getUserName());
		updated.setStatus(user.getStatus());

		for (Field f : User.class.getDeclaredFields()) {
			f.setAccessible(true);
			try {
				Object val = f.get(updated);
				if (val != null) {
					f.set(user, val);
				}
			} catch (Exception ignored) {
			}
		}

		userRepository.save(user);

		return ControllerTools.makeResponse(request.getExtra(), null, "success", 200);
	}

	@PostMapping("/join-app")
	public ResponseEntity<Response> joinApp(@RequestHeader(ControllerConstants.HEADER_USER_ID) String userName,
										   @RequestBody JoinAppRequest request) {

		User user = userRepository.findByUserName(userName);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		String appName = request.getBody().getAppName();
		App app = appRepository.findByName(appName);
		if (app == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "app not found", 404);
		}

		UserAppRole appRole = new UserAppRole()
				.setApp(app)
				.setUser(user)
				.setRole(app.getDefaultRole())
				.setId(UuidGeneratorUtil.generateUuid());
		userAppRoleRepository.save(appRole);

		return ControllerTools.makeResponse(request.getExtra(), null, "OK", 200);
	}

	private void sendMessage(String receiver, String message) {
		String msgType = null;
		if (receiver.startsWith("+")){
			msgType = SendMessageRequest.MSG_TYPE_SMS;
		} else if(receiver.contains("@")){
			msgType = SendMessageRequest.MSG_TYPE_MAIL;
		} else {
			throw new RuntimeException("Invalid receiver " + receiver);
		}
		try {
			template.postForEntity(messagingHubUrl + "/send",
				new SendMessageRequest(receiver, msgType, message), String.class);
		} catch (Exception e) {
			log.warn("Error while sending message", e);
		}
	}

	@GetMapping("/get-user-roles")
	public ResponseEntity<Response> getUserRoles(@RequestHeader(ControllerConstants.HEADER_USER_ID) String callerUserName,
												 @RequestParam("username") String userName, @RequestParam("app_name") String appName) {
		User callerUser = userRepository.findByUserName(callerUserName);
		if (callerUser == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "caller user not found", 404);
		}

		App app = appRepository.findByName(appName);
		if (app == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "app not found", 404);
		}

		// the caller user must have already joined the app, doesn't have to be admin
		if (userAppRoleRepository.findRolesByUserApp(callerUser, app).isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, "Access denied", 403);
		}

		User user = userRepository.findByUserName(userName);
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		return ControllerTools.makeResponse(new GetUserRolesResponse()
						.setApp_name(appName)
						.setUsername(userName)
						.setRoles(userAppRoleRepository
								.findRolesByUserApp(user, app).stream()
								.map(Role::getName).collect(Collectors.toList())),
				null, 200);
	}

	@PostMapping("/update-user-roles")
	public ResponseEntity<Response> updateUserRoles(@RequestHeader(ControllerConstants.HEADER_USER_ID) String callerUserName,
													@Valid @RequestBody UpdateUserAppRolesRequest request, BindingResult bindingResult) throws JsonProcessingException {

		User callerUser = userRepository.findByUserName(callerUserName);
		if (callerUser == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "caller user not found", 404);
		}

		String emptyFieldErrors = ControllerTools.checkForEmptyFields(bindingResult);
		if (!emptyFieldErrors.isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, emptyFieldErrors, 400);
		}

		UpdateUserAppRolesRequest.Body body = request.getBody();

		App app = appRepository.findByName(body.getApp_name());
		if (app == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "app not found", 404);
		}

		// the caller user must have already joined the app, doesn't have to be admin
		if (userAppRoleRepository.findRolesByUserApp(callerUser, app).isEmpty()) {
			return ControllerTools.makeResponse(request.getExtra(), null, "Access denied", 403);
		}

		// only admin of the same app can grant admin role
		if (Arrays.asList(body.getRoles()).contains(ControllerConstants.ADMIN_ROLE_NAME) &&
				userAppRoleRepository.findRolesByUserApp(callerUser, app).stream().noneMatch(uar -> uar.getName().equals(ControllerConstants.ADMIN_ROLE_NAME))) {
			return ControllerTools.makeResponse(request.getExtra(), null, "Only admin can grant admin role", 403);
		}

		User user = userRepository.findByUserName(body.getUsername());
		if (user == null) {
			return ControllerTools.makeResponse(request.getExtra(), null, "user not found", 404);
		}

		if (asList(body.getRoles()).stream().anyMatch(role -> roleRepository.findByName(role) == null)) {
			return ControllerTools.makeResponse(request.getExtra(), null, "one of roles not found", 400);
		}

		userAppRoleRepository.findUserAppRoleByAppAndUser(app, user).forEach(uap -> userAppRoleRepository.delete(uap));

		asList(body.getRoles()).stream().map(role -> roleRepository.findByName(role)).forEach(role -> {
			userAppRoleRepository.save(new UserAppRole().setId(UuidGeneratorUtil.generateUuid()).setRole(role).setUser(user).setApp(app));
		});

		return ControllerTools.makeResponse(request.getExtra(), null, "Roles are successfully updated", 200);
	}

	private String generateCode() {
		return String.format("%05d",random.nextInt(100000));
	}

	private void loginFailed(String userId) {
		Map<Integer, Long> history = loginHistory.entries(userId);
		int failuers = (history != null ? history.keySet().parallelStream().findFirst().get() : 0) + 1;
		long trottling = failuers == 1 ? System.currentTimeMillis() : System.currentTimeMillis() + (long)(1000 * Math.pow(2, failuers - 1));
		loginHistory.put(userId,failuers,trottling);
	}

	private boolean canLogin(String userId) {
		Map<Integer,Long> history = loginHistory.entries(userId);

		return history == null || System.currentTimeMillis() >= history.values().parallelStream().findFirst().get();
	}

	private void allowLogin(User user) {
		loginHistory.delete(user.getUserName());
		if (user.getEmail() != null){

			loginHistory.delete(user.getUserName());
		}
		if (user.getMobile() != null){
			loginHistory.delete(user.getModile());
		}
	}

	private int getThrottling(String userId) {
		Map<Integer,Long> history = loginHistory.entries(userId);
		if (history == null) {
			return 0;
		} else {

			return (int) ((history.values().parallelStream().findFirst().get() - System.currentTimeMillis()) / 1000);
		}
	}


}
