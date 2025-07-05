package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.security.JwtTokenService;
import com.github.switcherapi.ac.util.Roles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

@SpringBootTest
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class ApiResourcesTests {

	@Autowired JwtTokenService jwtService;
	@Autowired WebTestClient webTestClient;

	private static final String GITHUB_ID = String.format("[ApiResourcesTests]_github_id_%s", System.currentTimeMillis());
	private static Admin adminAccount;
	private static Authentication authentication;
	private String bearer;
	
	@BeforeAll
	static void setup(@Autowired AdminService adminService, @Autowired ReactiveAuthenticationManager authenticationManage) {
		adminAccount = adminService.createAdminAccount(GITHUB_ID).block();
		authentication = authenticationManage.authenticate(
				new UsernamePasswordAuthenticationToken(
						Objects.requireNonNull(adminAccount).getId(), GITHUB_ID,
						List.of(new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name())))).block();
	}
	
	@BeforeEach
	void setup(@Autowired AdminService adminService) {
		var token = jwtService.generateToken(authentication).getLeft();
		bearer = String.format("Bearer %s", token);

		StepVerifier.create(adminService.updateAdminAccountToken(adminAccount, token))
				.expectNextCount(1)
				.verifyComplete();
	}
	
	@Test
	void shouldNotAccessActuator() {
		webTestClient.get()
				.uri("/actuator")
				.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_KEY")
				.exchange()
				.expectStatus().isUnauthorized();
	}
	
	@Test
	void shouldAccessActuator() {
		this.webTestClient.get()
				.uri("/actuator")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	void shouldAccessSwagger() {
		webTestClient.get()
				.uri("/v3/api-docs")
				.exchange()
				.expectStatus().isOk();
	}

	@Test
	void shouldAccessSwaggerUI() {
		webTestClient.get()
				.uri("/swagger-ui/index.html")
				.exchange()
				.expectStatus().isOk();
	}

}
