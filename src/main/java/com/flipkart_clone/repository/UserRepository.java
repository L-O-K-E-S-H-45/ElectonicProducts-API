package com.flipkart_clone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart_clone.entities.User;
import com.flipkart_clone.enums.UserRole;
import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Integer> {
	
	Optional<User> findByUserName(String userName);

	boolean existsByEmail(String email);
	
	User findByEmail(String email);
	
	User findByUserRole(UserRole userRole);
	
}
