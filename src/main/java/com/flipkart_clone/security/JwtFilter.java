package com.flipkart_clone.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.flipkart_clone.entities.AccessToken;
import com.flipkart_clone.exception.UserNotFoundException;
import com.flipkart_clone.exception.UserNotLoggedInException;
import com.flipkart_clone.repository.AccessTokenRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class JwtFilter  extends OncePerRequestFilter{
	
	private AccessTokenRepository accessTokenRepo;
	
	private Jwtservice jwtservice;
	
	private CustomUserDetailsService userDetailsService;
	/**
	 * we cannot re-initialize the reference variable of another class inside lamda expression
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String at=null;
		String rt=null;
		Cookie[] cookies = request.getCookies();
		if (cookies!=null) {
			for (Cookie cookie:cookies) {
				if (cookie.getName().equals("at")) at=cookie.getValue();
				if (cookie.getName().equals("rt")) rt=cookie.getValue();
			}
			String username=null;
			if (at!=null && rt!=null) {
				Optional<AccessToken> accessToken = accessTokenRepo.findByTokenAndIsBlocked(at,false);
				
				if (accessToken==null) throw new UserNotLoggedInException("User not logged in");
				else {
					log.info("Authenticating the token....");
					username=jwtservice.extractUsername(at);
					if (username==null) throw new UserNotFoundException("Failed to authenticate");
					UserDetails userDetails = userDetailsService.loadUserByUsername(username);
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
							username, null,userDetails.getAuthorities());
					token.setDetails(new WebAuthenticationDetails(request)); // setting info related to web request
					SecurityContextHolder.getContext().setAuthentication(token);
					log.info("Authenticated successfully");
				}
			}
		}
		filterChain.doFilter(request, response); // To pass to different filter layers
	}
}
