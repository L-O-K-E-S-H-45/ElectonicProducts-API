package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.User;
import com.flipkart_clone.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByEmail(String email);
	
	User findByUserRole(UserRole userRole);
	
}
