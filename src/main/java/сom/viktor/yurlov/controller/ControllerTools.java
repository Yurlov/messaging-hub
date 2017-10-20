package сom.viktor.yurlov.controller;


import сom.viktor.yurlov.domain.rest.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.stream.Collectors;

public class ControllerTools {

	static ResponseEntity<Response> makeResponse(String extra, Object body, String message, int code) {
		if (extra == null) {
			return new ResponseEntity<>(new Response().setBody(body).setMessage(message).setCode(code), HttpStatus.valueOf(code));

		} else
			return new ResponseEntity<>(new Response().setBody(body).setMessage(message).setCode(code).setExtra(extra), HttpStatus.valueOf(code));


	}


	static String checkForEmptyFields(BindingResult bindingResult) {
		if (!bindingResult.hasErrors()) return "";

		return bindingResult.getFieldErrors().stream().map(fe -> fe.getField())
				.collect(Collectors.joining(", ")) + ": not specified.";
	}
}
