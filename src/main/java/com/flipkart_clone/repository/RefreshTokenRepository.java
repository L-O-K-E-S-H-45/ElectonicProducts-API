package com.flipkart_clone.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.RefreshToken;
import com.flipkart_clone.entities.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

		Optional<RefreshToken> findByToken(String token);
		
		List<RefreshToken> findAllByExpirationBefore(LocalDateTime dateTime);
		
		List<RefreshToken> findAllByUserAndIsBlocked(User user, boolean isBlocked);

		List<RefreshToken> findAllByUserAndIsBlockedAndTokenNot(User user, boolean b, String refreshToken);
	
}
