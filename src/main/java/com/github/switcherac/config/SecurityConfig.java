package com.github.switcherac.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value("${service.endpoint.allowed-addresses}")
	private String allowedAddresses;
	
	@Value("${service.endpoint.healthchecker}")
	private String healthChecker;
	
	@Autowired
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(healthChecker).permitAll()
				.antMatchers("/**").access("isAuthenticated() and (" + buildAllowedIpList() + ")")
			
			.and()
				.exceptionHandling()
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			
			.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			
			.and()
				.csrf().disable();
		
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	private String buildAllowedIpList() {
		final String[] addresses = allowedAddresses.split(",");
		final String accessIpAddress = Arrays.stream(addresses)
				.map(address -> "hasIpAddress('" + address.trim() + "') or ")
				.collect(Collectors.joining());

		return accessIpAddress.substring(0, accessIpAddress.length() - 4);
	}

}
