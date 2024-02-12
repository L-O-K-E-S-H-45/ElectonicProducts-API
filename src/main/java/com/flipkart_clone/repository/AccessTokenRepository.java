package com.flipkart_clone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.AccessToken;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
	
	Optional<AccessToken> findByToken(String token);
}
