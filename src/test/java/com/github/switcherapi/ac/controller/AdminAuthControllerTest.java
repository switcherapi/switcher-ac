package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.repository.AdminRepository;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.security.JwtTokenService;
import com.github.switcherapi.ac.util.Roles;
import com.github.switcherapi.client.test.SwitcherTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
class AdminAuthControllerTest {

	@Autowired AdminRepository adminRepository;
	@Autowired JwtTokenService jwtTokenService;
	@Autowired WebTestClient webTestClient;

	private static final String GITHUB_ID = String.format("[AdminAuthControllerTests]_github_id_%s", System.currentTimeMillis());
	private static Admin adminAccount;
	private static Authentication authentication;
	private Pair<String, String> tokens;

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
		tokens = jwtTokenService.generateToken(authentication);
		StepVerifier.create(adminService.updateAdminAccountToken(adminAccount, tokens.getLeft()))
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	void shouldRefreshToken() {
		var response = webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", tokens.getRight())
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(GitHubAuthDTO.class)
				.returnResult()
				.getResponseBody();

		assertNotNull(response);
		assertThat(response.admin().gitHubId()).isEqualTo(GITHUB_ID);
		assertThat(response.token()).isNotEqualTo(tokens.getLeft());
		assertThat(response.refreshToken()).isNotEqualTo(tokens.getRight());
	}

	@Test
	void shouldNotRefreshToken_invalidToken() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", tokens.getRight())
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_TOKEN")
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotRefreshToken_missingToken() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", tokens.getRight())
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@Test
	void shouldNotRefreshToken_missingRefreshToken() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().is4xxClientError();
	}

	@SwitcherTest(key = "SWITCHER_AC_ADM", result = false)
	void shouldNotRefreshToken_accountUnauthorized() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", tokens.getRight())
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotRefreshToken_invalidRefreshToken() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", "INVALID_REFRESH_TOKEN")
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotRefreshToken_invalidRefreshTokenPayload() {
		var previousToken = tokens.getLeft();
		tokens = jwtTokenService.generateToken(authentication);

		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/refresh")
						.queryParam("refreshToken", tokens.getRight())
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + previousToken)
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldLogout() {
		webTestClient.post()
				.uri("/admin/v1/logout")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk();

		var admin = adminRepository.findByGitHubId(GITHUB_ID).block();
		assertNotNull(admin);
		assertThat(admin.getToken()).isNull();
	}
	
}
