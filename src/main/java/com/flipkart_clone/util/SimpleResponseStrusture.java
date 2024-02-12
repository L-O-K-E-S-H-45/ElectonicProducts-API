package com.flipkart_clone.util;

import org.springframework.stereotype.Component;

@Component
public class SimpleResponseStrusture<T> {
	
	private int status;
	private String message;
	
	public int getStatus() {
		return status;
	}
	public SimpleResponseStrusture<T> setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public SimpleResponseStrusture<T> setMessage(String message) {
		this.message = message;
		return this;
	}

}
