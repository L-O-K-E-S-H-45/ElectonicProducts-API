package com.flipkart_clone.service;

import org.springframework.http.ResponseEntity;

import com.flipkart_clone.requestdtos.OTPModdel;
import com.flipkart_clone.requestdtos.UserRequest;
import com.flipkart_clone.responsedtos.UserResponse;
import com.flipkart_clone.util.ResponseStructure;

public interface AuthService {

	ResponseEntity<ResponseStructure<String>> registerUser(
			UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OTPModdel otpModdel);

}
