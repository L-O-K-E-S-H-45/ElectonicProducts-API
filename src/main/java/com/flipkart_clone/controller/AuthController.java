package com.flipkart_clone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.service.AuthService;
import com.flipkart_clone.util.ResponseStructure;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/flipkart-v1")
@AllArgsConstructor
public class AuthController {
	
//	@Autowired // (this will do field injection, not good practice, better use constructor injection)
//	private AuthService authService;
	
	private AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> registerUser(
			@RequestBody @Valid UserRequest userRequest){
		return authService.registerUser(userRequest);
	}
	
	
}
