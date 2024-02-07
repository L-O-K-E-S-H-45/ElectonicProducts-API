package com.flipkart_clone.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.flipkart_clone.exception.UserNotFoundByEmailException;
import com.flipkart_clone.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return new CustomUserDetails(userRepo.findByUserName(username)
				.orElseThrow(()-> new UserNotFoundByEmailException("Failed to Authenticate user!!!")));
	}

}
