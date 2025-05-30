package com.github.switcherapi.ac.service.security;

import com.github.switcherapi.ac.util.Roles;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class JwtTokenAuthenticationFilter implements WebFilter {

	public static final String SWITCHER_AC = "SWITCHER_AC";
	public static final String HEADER_PREFIX = "Bearer ";

	private final JwtTokenService jwtTokenService;
	private final Map<Roles, SimpleGrantedAuthority> grantedAuthorityMap;

	public JwtTokenAuthenticationFilter(JwtTokenService jwtTokenService,
									   Map<Roles, SimpleGrantedAuthority> grantedAuthorityMap) {
		this.jwtTokenService = jwtTokenService;
		this.grantedAuthorityMap = grantedAuthorityMap;
	}

	@Override
	@NonNull
	public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
		final var token = resolveToken(exchange.getRequest());

		if (StringUtils.isBlank(token) || isRefreshTokenRequest(exchange.getRequest())) {
			return chain.filter(exchange);
		}

		if (isSwitcherRequest(exchange.getRequest())) {
			return filterSwitcherRequest(exchange, chain, token);
		}

		return jwtTokenService.validateToken(token)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token")))
				.flatMap(admin -> filterAdminRequest(exchange, chain, token));
	}

	private Mono<Void> filterSwitcherRequest(ServerWebExchange exchange, WebFilterChain chain, String token) {
		if (!jwtTokenService.validateRelayToken(token)) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid relay token"));
		}

		var authSwitcherUser = new UsernamePasswordAuthenticationToken(
				SWITCHER_AC, null,
				List.of(grantedAuthorityMap.get(Roles.ROLE_SWITCHER)));

		return Mono.fromCallable(() -> authSwitcherUser)
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(authentication -> chain.filter(exchange)
						.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
	}

	private Mono<Void> filterAdminRequest(ServerWebExchange exchange, WebFilterChain chain, String token) {
		return Mono.fromCallable(() -> jwtTokenService.getAuthentication(token))
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(authentication -> chain.filter(exchange)
						.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
	}

	private String resolveToken(ServerHttpRequest request) {
		final var bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (StringUtils.isNotBlank(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
			return bearerToken.substring(7);
		}

		return null;
	}

	private boolean isSwitcherRequest(ServerHttpRequest request) {
		return request.getPath().value().startsWith("/switcher");
	}

	private boolean isRefreshTokenRequest(ServerHttpRequest request) {
		return request.getPath().value().startsWith("/admin/v1/auth/refresh");
	}

}