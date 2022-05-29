package com.github.switcherapi.ac.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Value("${service.endpoint.healthchecker}")
	private String healthChecker;
	
	@Autowired
	private JwtRequestFilter jwtRequestFilter;
	
    private static final String[] SWAGGER_MATCHERS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };
	
	public enum Roles {
		ADMIN, ROLE_ADMIN,
		SWITCHER, ROLE_SWITCHER
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers(healthChecker).permitAll()
				.antMatchers("/admin/v1/auth/**").permitAll()
				.antMatchers("/actuator/**").hasRole(Roles.ADMIN.name())
				.antMatchers("/admin/**").hasRole(Roles.ADMIN.name())
				.antMatchers("/switcher/**").hasRole(Roles.SWITCHER.name())
				.antMatchers(SWAGGER_MATCHERS).authenticated()
					.and().httpBasic().authenticationEntryPoint(authenticationEntryPoint())
			
			.and()
				.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint())
			
			.and()
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				
			.and()
				.cors().and().csrf().disable();
		
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
	}
	
	@Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        final var entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("admin realm");
        return entryPoint;
    }

}
