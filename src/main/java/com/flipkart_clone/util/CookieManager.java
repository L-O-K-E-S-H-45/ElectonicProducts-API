package com.flipkart_clone.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;

@Component
public class CookieManager {
	
	@Value("${myapp.domain}")
	private String domain;
	
	/**
	 * this method can be private in AuthServiceImplementaion class but 
	 * to use somewhere whenever needed, writing here as public method
	 */
	public Cookie configure(Cookie cookie, int expirationInseconds) {
		cookie.setDomain(domain);
		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(expirationInseconds);
		return cookie;
	}
	
	public Cookie invalidate(Cookie cookie) {
		cookie.setPath("/");
		cookie.setMaxAge(0);
		return cookie;
	}

}






