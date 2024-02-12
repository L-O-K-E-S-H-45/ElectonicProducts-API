package com.flipkart_clone.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.AccessToken;
import com.flipkart_clone.entities.User;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
	
	Optional<AccessToken> findByToken(String token);
	
	List<AccessToken> findAllByExpirationBefore(LocalDateTime dateTime);
	
	Optional<AccessToken> findByTokenAndIsBlocked(String token, boolean isBlocked);
	
	List<AccessToken> findAllByUserAndIsBlocked(User user, boolean isBlocked);

	List<AccessToken> findAllByUserAndIsBlockedAndTokenNot(User user, boolean b, String accessToken);
}
