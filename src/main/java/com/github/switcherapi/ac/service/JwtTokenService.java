package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.repository.AdminRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class JwtTokenService {
	
	public static final int JWT_TOKEN_VALIDITY = 5 * 60 * 1000;

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

	/**
	 * Generates a refresh token for a given token.
	 *
	 * @param token - the token to be refreshed
	 * @return a refresh token
	 */
	private String generateRefreshToken(String token) {
		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var claims = Jwts.claims()
				.subject(token.substring(token.length() - 8)).build();

		return Jwts.builder().claims(claims).signWith(key).compact();
	}

	/**
	 * Generates a pair of token (token and refreshToken) for a given subject
	 * in which the expiration date is the current date plus the JWT_TOKEN_VALIDITY.
	 *
	 * @param subject - the subject of the token
	 * @return a pair of token (token and refreshToken)
	 */
	public Pair<String, String> generateToken(String subject) {
		log.debug("Generating token for {}", subject);

		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var claims = Jwts.claims()
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.subject(subject).build();

		final var token = Jwts.builder().claims(claims).signWith(key).compact();

		return Pair.of(token, generateRefreshToken(token));
	}

	/**
	 * Refreshes a token given a subject, a token and a refresh token.
	 *
	 * @param subject - the subject of the token
	 * @param token - the token to be refreshed
	 * @param refreshToken - the refresh token
	 * @return a pair of token (token and refreshToken)
	 */
	public Pair<String, String> refreshToken(String subject, String token, String refreshToken) {
		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var jwtParser = Jwts.parser().verifyWith(key).build();
		final var refreshSubject = jwtParser.parseSignedClaims(refreshToken).getPayload().getSubject();

		if (refreshSubject.equals(token.substring(token.length() - 8))) {
			return generateToken(subject);
		}

		log.debug("Refresh token could not be processed for {}", subject);
		return null;
	}

	/**
	 * Validates token based on subject in the payload.
	 *
	 * @param token - the token to be validated
	 * @return the subject of the token if it is valid, otherwise an empty string
	 */
	public String validateToken(String token) {
		try {
			final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
			final var jwtParser = Jwts.parser().verifyWith(key).build();
			final var subject = jwtParser.parseSignedClaims(token).getPayload().getSubject();

			final var admin = adminRepository.findByToken(token).block();
			return Objects.nonNull(admin) ? subject : null;
		} catch (JwtException e) {
			log.error("Failed to validate JWT - {}", e.getMessage());
			return StringUtils.EMPTY;
		}
	}
	
}