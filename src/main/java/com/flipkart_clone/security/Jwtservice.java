package com.flipkart_clone.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class Jwtservice {
 
	@Value("${myapp.secret}")
	private String secret;
	
	@Value("${myapp.access.expiry}")
	private Long accessExpirationInseconds;
	
	@Value("${myapp.refresh.expiry}")
	private Long refreshExpirationInseconds;
	
	public String generateAccessToken(String username) {
		return generateJwt(new HashMap<String, Object>(), username, accessExpirationInseconds*1000l);
	}
	
	public String generateRefreshToken(String username) {
		return generateJwt(new HashMap<String,Object>(), username, refreshExpirationInseconds*1000l);
	}
	
	private String generateJwt(Map<String, Object> claims, String username, Long expiry) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiry))
				.signWith(getSignature(), SignatureAlgorithm.HS512) // signing the JWT with key
				.compact();
	}
	
	private Key getSignature() {
		byte[] secretKeys=Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(secretKeys);
	}
	
	public Claims parseJwt(String token) {
		System.out.println("token 1111 "+token);
		 JwtParser jwtParser =Jwts.parserBuilder().setSigningKey(getSignature()).build();
		 return jwtParser.parseClaimsJws(token).getBody();
	}
	
	public String extractUsername(String token) {
		System.out.println("token 2222 "+token);
		return parseJwt(token).getSubject();
	}

}












