package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.AccessToken;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

}
