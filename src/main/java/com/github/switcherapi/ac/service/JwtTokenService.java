package com.github.switcherapi.ac.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.switcherapi.ac.repository.AdminRepository;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {
	
	private static final Logger logger = LogManager.getLogger(JwtTokenService.class);
	
	public static final int JWT_TOKEN_VALIDITY = 5; //min
	
	@Value("${service.api.secret}")
	private String jwtSecret;
	
	@Value("${service.relay.token}")
	private String relayToken;
	
	@Autowired
	private AdminRepository adminRepository;
	
	public boolean validateAdminToken(String token) {
		return StringUtils.isNotBlank(validateToken(token));
	}
	
	public boolean validateRelayToken(String token) {
		return relayToken.equals(token);
	}
	
	public String[] generateToken(String subject) {
		if (logger.isDebugEnabled()) {
			logger.debug("Generating token for {}", subject);
		}
		
		final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		final String token = Jwts.builder()
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 60 * 1000))
				.setSubject(subject)
				.signWith(key)
				.compact();
		
		return new String[] { token, generateRefreshToken(token) };
	}
	
	public String[] refreshToken(String subject, String token, String refreshToken) {
		final var crypt = new BCryptPasswordEncoder();
		
		if (token != null && crypt.matches(token.split("\\.")[2], refreshToken)) {
			return generateToken(subject);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Refresh token could not be processed for {}", subject);
		}
		
		return ArrayUtils.EMPTY_STRING_ARRAY;
	}
	
	public String validateToken(String token) {
		try {
			final String subject = 
					Jwts.parserBuilder()
						.setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8)).build()
						.parseClaimsJws(token).getBody().getSubject();
			
			final var adminAccount = adminRepository.findByToken(token);
			return adminAccount != null ? subject : null;
		} catch (JwtException e) {
			logger.error("Failed to validate JWT - {}", e.getMessage());
			return StringUtils.EMPTY;
		}
	}
	
	private String generateRefreshToken(String token) {
		return new BCryptPasswordEncoder().encode(token.split("\\.")[2]);
	}
}