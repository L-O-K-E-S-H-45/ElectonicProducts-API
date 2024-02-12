package com.flipkart_clone.util;

import org.springframework.stereotype.Component;

@Component
public class SimpleResponseStrusture {
	
	private int status;
	private String message;
	
	public int getStatus() {
		return status;
	}
	public SimpleResponseStrusture setStatus(int status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public SimpleResponseStrusture setMessage(String message) {
		this.message = message;
		return this;
	}

}
