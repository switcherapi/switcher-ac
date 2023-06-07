package com.github.switcherapi.ac.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final String healthChecker;

	private final JwtRequestFilter jwtRequestFilter;
	
    private static final String[] SWAGGER_MATCHERS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };

	public SecurityConfig(
			@Value("${service.endpoint.healthchecker}") String healthChecker,
			JwtRequestFilter jwtRequestFilter) {
		this.healthChecker = healthChecker;
		this.jwtRequestFilter = jwtRequestFilter;
	}

	public enum Roles {
		ADMIN, ROLE_ADMIN,
		SWITCHER, ROLE_SWITCHER
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth ->
				auth.requestMatchers(healthChecker).permitAll()
					.requestMatchers("/admin/v1/auth/**").permitAll()
					.requestMatchers("/actuator/**").hasRole(Roles.ADMIN.name())
					.requestMatchers("/admin/**").hasRole(Roles.ADMIN.name())
					.requestMatchers("/plan/**").hasRole(Roles.ADMIN.name())
					.requestMatchers("/switcher/**").hasRole(Roles.SWITCHER.name())
					.requestMatchers(SWAGGER_MATCHERS).authenticated());

		http.httpBasic(auth -> auth.authenticationEntryPoint(authenticationEntryPoint()));
		http.exceptionHandling(auth -> auth.authenticationEntryPoint(authenticationEntryPoint()));
		http.sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.csrf(AbstractHttpConfigurer::disable);

		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
	
	@Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        final var entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("admin realm");
        return entryPoint;
    }

}
