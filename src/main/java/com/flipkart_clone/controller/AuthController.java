package com.flipkart_clone.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flipkart_clone.requestdtos.AuthRequest;
import com.flipkart_clone.requestdtos.OTPModdel;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.AuthResponse;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.service.AuthService;
import com.flipkart_clone.util.ResponseStructure;
import com.flipkart_clone.util.SimpleResponseStrusture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	public ResponseEntity<ResponseStructure<String>> registerUser(
			@RequestBody @Valid UserRequest userRequest){
		return authService.registerUser(userRequest);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody OTPModdel otpModdel){
		return authService.verifyOTP(otpModdel);
	}
	
	@PostMapping("/login") // HttpServletResponse is generated by spring security filter for every request, here v r not giving input
//	@PreAuthorize("hasAuthority('SELLER', 'CUSTOMER')")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest, 
			HttpServletResponse response){
		return authService.login(authRequest,response);
	}
	
	@PostMapping("/traditional-logout")
	public ResponseEntity<ResponseStructure<String>> traditionalLogout(HttpServletRequest request, HttpServletResponse response){
		return authService.traditionalLogout(request,response);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<SimpleResponseStrusture> logout(@CookieValue(name = "at", required = false) 
		String accessToken, @CookieValue(name = "rt", required = false) String refreshToken, HttpServletResponse response){
		return authService.logout(accessToken, refreshToken,response);
	}
	
	@PostMapping("/revoke-all")
	public ResponseEntity<SimpleResponseStrusture> revokeAllDevices(@CookieValue(name = "at", required = false) 
	String accessToken, @CookieValue(name = "rt", required = false) String refreshToken, HttpServletResponse response){
		return authService.revokeAllDevices(accessToken,refreshToken,response);
	}
	
	@PostMapping("/revoke")
	public ResponseEntity<SimpleResponseStrusture> revokeOtherDevices(@CookieValue(name = "at", required = false) 
	String accessToken, @CookieValue(name = "rt", required = false) String refreshToken, HttpServletResponse response){
		return authService.revokeOtherDevices(accessToken,refreshToken,response);
	}
	
	@PostMapping("/refresh-login")
	public ResponseEntity<SimpleResponseStrusture> refreshLogin(HttpServletRequest request, HttpServletResponse response){
		return authService.refreshLogin(request,response);
	}
	
}
