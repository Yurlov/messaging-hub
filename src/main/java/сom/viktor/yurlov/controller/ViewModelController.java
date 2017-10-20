package сom.viktor.yurlov.controller;

import javax.validation.Valid;

import gtedx.hylaa.iam.domain.rest.*;
import om.viktor.yurlov.domain.rest.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import сom.viktor.yurlov.domain.rest.*;


@Controller
public class ViewModelController {

	private final String secretKey;
	private final UserController userController;


	public ViewModelController(@Value("${iam.secret-key}") String secretKey,
							   UserController userController) {
		this.secretKey = secretKey;
		this.userController = userController;
	}

	@GetMapping("/ui/register")
	public String registerForm(Model model) {
		model.addAttribute("registerRequest", new RegisterRequest());
		return "register";
	}

	@PostMapping("/ui/register")
	public String registerForm(@Valid RegisterRequest registerRequest,
							   Model model,
							   BindingResult bindingResult) {

		ResponseEntity<Response> responseEntity = userController.register(registerRequest, bindingResult);
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}

	@GetMapping("/ui/verify")
	public String verifyForm(Model model) {
		model.addAttribute("verifyRequest", new VerifyRequest());
		return "verify";
	}

	@PostMapping("/ui/verify")
	public String verifyForm(@Valid VerifyRequest verifyRequest,
							Model model,
							BindingResult bindingResult) {

		ResponseEntity<Response> responseEntity = userController.verify(verifyRequest, bindingResult);
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}

	@GetMapping("/ui/login")
	public String loginForm(Model model) {
		model.addAttribute("loginRequest", new LoginRequest());
		return "login";
	}

	@PostMapping("/ui/login")
	public String loginForm(@Valid LoginRequest loginRequest,
							Model model,
							BindingResult bindingResult) {

		ResponseEntity<Response> responseEntity = userController.login(loginRequest, bindingResult);
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}

	@GetMapping("/ui/reset-password-request")
	public String resetPasswordRequest(Model model) {
		model.addAttribute("resetPasswordRequest", new ResetPasswordRequest());
		return "reset-password-request";
	}

	@PostMapping("/ui/reset-password-request")
	public String resetPasswordForm(@Valid ResetPasswordRequest resetPasswordRequest,
									Model model,
									BindingResult bindingResult) {

		ResponseEntity<Response> responseEntity = userController.resetPasswordRequest(resetPasswordRequest, bindingResult);
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}

	@GetMapping("/ui/reset-password")
	public String resetPassword(@RequestParam(value = "code", required = false) String code,
								Model model) {
		model.addAttribute("code", code);
		String userId = "";
		if (code != null){
			userId = Code.decrypt(code, secretKey).userId();
		}
		model.addAttribute("userId", userId);
		model.addAttribute("resetPasswordRequest", new ResetPasswordConfirmRequest());
		return "reset-password";
	}

	@PostMapping("/ui/reset-password")
	public String resetPasswordForm(@RequestParam(value = "code", required = false) String code,
									@Valid ResetPasswordConfirmRequest resetPasswordConfirmRequest,
									Model model,
									BindingResult bindingResult) {
		ResponseEntity<Response> responseEntity = null;
		if(code != null){
			responseEntity = userController.resetPasswordByLink(resetPasswordConfirmRequest, code, bindingResult);
		} else {
			responseEntity = userController.resetPasswordByCode(resetPasswordConfirmRequest, bindingResult);
		}
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}

	@GetMapping("/ui/activate")
	public String loginForm(@RequestParam("code") String code,
							Model model) {
		ResponseEntity<Response> responseEntity = userController.activate(code);
		model.addAttribute("response", responseEntity.getBody());
		return "response";
	}
}