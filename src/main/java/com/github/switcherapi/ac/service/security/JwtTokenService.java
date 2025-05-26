package com.github.switcherapi.ac.service.security;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.repository.AdminRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtTokenService {

	private static final String AUTHORITIES_KEY = "roles";

	public static final int JWT_TOKEN_VALIDITY = 60 * 1000;

	private final AdminRepository adminRepository;

	private final String jwtSecret;

	private final String relayToken;

	public JwtTokenService(
			@Value("${service.api.secret}") String jwtSecret,
			@Value("${service.relay.token}") String relayToken,
			AdminRepository adminRepository) {
		this.jwtSecret = jwtSecret;
		this.relayToken = relayToken;
		this.adminRepository = adminRepository;
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
	 * @param authentication - the authentication object containing the principal and authorities
	 * @return a pair of token (token and refreshToken)
	 */
	public Pair<String, String> generateToken(Authentication authentication) {
		log.debug("Generating token for {}", authentication.getName());

		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var claims = Jwts.claims()
				.issuedAt(new Date())
				.id(UUID.randomUUID().toString())
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
				.add(AUTHORITIES_KEY, authentication.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.collect(Collectors.joining(",")))
				.subject(authentication.getName()).build();

		final var token = Jwts.builder().claims(claims).signWith(key).compact();

		return Pair.of(token, generateRefreshToken(token));
	}

	/**
	 * Refreshes a token given a subject, a token and a refresh token.
	 *
	 * @param authentication - the authentication object containing the principal and authorities
	 * @param token - the token to be refreshed
	 * @param refreshToken - the refresh token
	 * @return a pair of token (token and refreshToken)
	 */
	public Pair<String, String> refreshToken(Authentication authentication, String token, String refreshToken) {
		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var jwtParser = Jwts.parser().verifyWith(key).build();
		final var refreshSubject = jwtParser.parseSignedClaims(refreshToken).getPayload().getSubject();

		if (refreshSubject.equals(token.substring(token.length() - 8))) {
			return generateToken(authentication);
		}

		log.debug("Refresh token could not be processed for {}", authentication.getName());
		return null;
	}

	/**
	 * Validates token based on subject in the payload.
	 *
	 * @param token - the token to be validated
	 * @return the subject of the token if it is valid, otherwise an empty string
	 */
	public Mono<Admin> validateToken(String token) {
		try {
			final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
			final var jwtParser = Jwts.parser().verifyWith(key).build();
			final var subject = jwtParser.parseSignedClaims(token).getPayload().getSubject();

			log.debug("Validating token for subject: {}", subject);
			return adminRepository.findByToken(token);
		} catch (JwtException e) {
			log.error("Failed to validate JWT - {}", e.getMessage());
			return Mono.empty();
		}
	}

	public Authentication getAuthentication(String token) {
		final var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
		final var jwtParser = Jwts.parser().verifyWith(key).build();
		final var claims = jwtParser.parseSignedClaims(token).getPayload();

		var authoritiesClaim = claims.get(AUTHORITIES_KEY);
		var authorities = authoritiesClaim == null
				? AuthorityUtils.NO_AUTHORITIES
				: AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

		var principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}
	
}