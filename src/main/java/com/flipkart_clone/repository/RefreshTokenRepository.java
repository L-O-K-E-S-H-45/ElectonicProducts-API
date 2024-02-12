package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.AccessToken;
import com.flipkart_clone.entities.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
