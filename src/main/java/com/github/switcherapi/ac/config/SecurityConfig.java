package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.repository.AdminRepository;
import com.github.switcherapi.ac.service.security.JwtTokenAuthenticationFilter;
import com.github.switcherapi.ac.service.security.JwtTokenService;
import com.github.switcherapi.ac.util.Roles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	Map<Roles, SimpleGrantedAuthority> grantedAuthorities() {
		final var roles = new EnumMap<Roles, SimpleGrantedAuthority>(Roles.class);
		roles.put(Roles.ROLE_ADMIN, new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name()));
		roles.put(Roles.ROLE_SWITCHER, new SimpleGrantedAuthority(Roles.ROLE_SWITCHER.name()));
		return Collections.unmodifiableMap(roles);
	}

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http,
												JwtTokenService jwtTokenService,
												ReactiveAuthenticationManager reactiveAuthenticationManager) {
		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
				.authenticationManager(reactiveAuthenticationManager)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
				.authorizeExchange(it -> it
						.pathMatchers("/error").permitAll()
						.pathMatchers("/admin/v1/auth/**").permitAll()
						.pathMatchers("/switcher/**").hasRole(Roles.SWITCHER.name())
						.pathMatchers("/actuator/**").hasRole(Roles.ADMIN.name())
						.pathMatchers("/admin/**").hasRole(Roles.ADMIN.name())
						.pathMatchers("/plan/**").hasRole(Roles.ADMIN.name())
						.anyExchange().permitAll()
				)
				.addFilterAt(new JwtTokenAuthenticationFilter(jwtTokenService, grantedAuthorities()), SecurityWebFiltersOrder.HTTP_BASIC)
				.build();
	}

	@Bean
	public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,
																	   PasswordEncoder passwordEncoder) {
		var authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		authenticationManager.setPasswordEncoder(passwordEncoder);
		return authenticationManager;
	}

	@Bean
	public ReactiveUserDetailsService userDetailsService(AdminRepository adminRepository,
														 PasswordEncoder passwordEncoder) {
		return id -> adminRepository.findById(id)
				.map(admin -> User
						.withUsername(admin.getId()).password(passwordEncoder.encode(admin.getGitHubId()))
						.authorities(grantedAuthorities().get(Roles.ROLE_ADMIN))
						.build()
				);
	}
}
