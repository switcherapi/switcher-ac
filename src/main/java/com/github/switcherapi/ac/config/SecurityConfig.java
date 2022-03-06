package com.github.switcherapi.ac.config;

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
				.antMatchers("/admin/auth/**").permitAll()
				.antMatchers("/actuator/**").hasRole("ADMIN")
				.antMatchers("/admin/**").hasRole("ADMIN")
				.antMatchers("/switcher/**").hasRole("SWITCHER")
			
			.and()
				.exceptionHandling()
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
			
			.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				
			.and()
				.cors().and().csrf().disable();
		
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}

}
