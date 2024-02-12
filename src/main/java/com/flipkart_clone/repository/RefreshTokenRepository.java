package com.flipkart_clone.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.AccessToken;
import com.flipkart_clone.entities.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

		Optional<RefreshToken> findByToken(String token);
	
}
