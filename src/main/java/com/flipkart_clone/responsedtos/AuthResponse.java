package com.flipkart_clone.responsedtos;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	
	private int userId;
	private String username;
	private String role;
	private boolean isAuthenticated;
	private LocalDateTime accessExpiration;
	private LocalDateTime refreshExpiration;

}
