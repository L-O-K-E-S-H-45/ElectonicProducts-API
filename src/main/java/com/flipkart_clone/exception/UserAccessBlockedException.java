package com.flipkart_clone.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserAccessBlockedException extends RuntimeException {
	
	private String message;
	
}
