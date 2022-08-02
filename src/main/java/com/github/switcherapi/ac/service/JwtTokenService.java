package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.repository.AdminRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService {
	
	private static final Logger logger = LogManager.getLogger(JwtTokenService.class);
	
	public static final int JWT_TOKEN_VALIDITY = 5; //min

	private final String jwtSecret;

	private final String relayToken;

	private final AdminRepository adminRepository;

	public JwtTokenService(
			@Value("${service.api.secret}") String jwtSecret,
			@Value("${service.relay.token}") String relayToken,
			AdminRepository adminRepository) {
		this.jwtSecret = jwtSecret;
		this.relayToken = relayToken;
		this.adminRepository = adminRepository;
	}

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
		final String refreshSubject = 
				Jwts.parserBuilder()
					.setSigningKey(jwtSecret.getBytes(StandardCharsets.UTF_8)).build()
					.parseClaimsJws(refreshToken).getBody().getSubject();
		
		if (token != null && refreshSubject.equals(token.substring(token.length() - 8))) {
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
		final SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
					.setSubject(token.substring(token.length() - 8))
					.signWith(key)
					.compact();
	}
	
}