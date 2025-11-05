package com.selimhorri.app.jwt.service;

import java.util.Date;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {
	
	String extractUsername(final String token);
	String extractUserId(final String token);
	Date extractExpiration(final String token);
	<T> T extractClaims(final String token, final Function<Claims, T> claimsResolver);
	String generateToken(final UserDetails userDetails, final String userId);
	Boolean validateToken(final String token, final UserDetails userDetails);
	
}










