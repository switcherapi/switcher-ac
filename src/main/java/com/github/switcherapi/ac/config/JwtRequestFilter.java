package com.github.switcherapi.ac.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.switcherapi.ac.service.JwtTokenService;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
	
	private static final Logger jwtLogger = LogManager.getLogger(JwtRequestFilter.class);
	
	public static final String SWITCHER_AC = "SWITCHER_AC";
	
	public static final String AUTHORIZATION = "Authorization";

	public static final String BEARER = "Bearer ";
	
	@Autowired
	private JwtTokenService jwtTokenService;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response, 
			FilterChain filterChain) throws ServletException, IOException {
		
		final Optional<String> jwt = getJwtFromRequest(request);
		
		jwt.ifPresent(token -> {
			if (jwtLogger.isDebugEnabled()) {
				jwtLogger.debug("Token {}", token);
			}
			
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			if (validateToken(token, request, authorities)) {
				final var authUser = new UsernamePasswordAuthenticationToken(SWITCHER_AC, null, authorities);
				authUser.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authUser);
			}
		});
		
		filterChain.doFilter(request, response);
	}
	
	/**
	 * Validate token given the accessed resource
	 */
	private boolean validateToken(String token, HttpServletRequest request, 
			List<SimpleGrantedAuthority> authorities) {
		
		if (request.getRequestURI().startsWith("/switcher")) {
			if (jwtTokenService.validateRelayToken(token)) {
				authorities.add(new SimpleGrantedAuthority("ROLE_SWITCHER"));
				return true;
			}
		} else {
			if (jwtTokenService.validateAdminToken(token)) {
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				return true;
			}
		}
		
		return false;
	}
	
	private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
		final String bearerToken = request.getHeader(AUTHORIZATION);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
			return Optional.of(bearerToken.substring(7));
		}
		return Optional.empty();
	}

}