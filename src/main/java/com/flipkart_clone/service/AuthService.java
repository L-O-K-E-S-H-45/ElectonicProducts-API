package com.flipkart_clone.service;

import org.springframework.http.ResponseEntity;

import com.flipkart_clone.requestdtos.AuthRequest;
import com.flipkart_clone.requestdtos.OTPModdel;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.AuthResponse;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.util.ResponseStructure;
import com.flipkart_clone.util.SimpleResponseStrusture;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<String>> registerUser(
			UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OTPModdel otpModdel);

	ResponseEntity<ResponseStructure<AuthResponse>> login(String accessToken,String refreshToken ,AuthRequest authRequest,HttpServletResponse response);

	ResponseEntity<ResponseStructure<String>> traditionalLogout(HttpServletRequest request, HttpServletResponse response);

	ResponseEntity<SimpleResponseStrusture> logout(String accessToken, String refreshToken, HttpServletResponse response);

	ResponseEntity<SimpleResponseStrusture> revokeOtherDevices(String accessToken, String refreshToken, HttpServletResponse response);

	ResponseEntity<SimpleResponseStrusture> revokeAllDevices(String accessToken, String refreshToken,
			HttpServletResponse response);

	ResponseEntity<SimpleResponseStrusture> refreshLogin(String accessToken, String refreshToken, 
			HttpServletResponse response);
	
}
