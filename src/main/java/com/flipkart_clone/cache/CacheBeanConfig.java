package com.flipkart_clone.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flipkart_clone.entities.User;

@Configuration
public class CacheBeanConfig {

	@Bean
	CacheStore<User> userCacheStore(){
		return new CacheStore<>(Duration.ofMinutes(5));
	}
	
	@Bean
	CacheStore<Integer> otpCacheStore(){
		return new CacheStore<Integer>(Duration.ofMinutes(3));
	}
	
}
