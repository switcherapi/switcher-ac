package com.github.switcherapi.ac.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.GitHubDetail;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class GitHubServiceTest {

	private final ObjectMapper mapper = new ObjectMapper();

	public static MockWebServer mockBackend;

	private GitHubService gitHubService;

	@BeforeAll
	static void setup() throws IOException {
		mockBackend = new MockWebServer();
		mockBackend.start();
	}

	@AfterAll
	static void tearDown() throws IOException {
		mockBackend.shutdown();
	}

	@BeforeEach
	void initialize() {
		var baseUrl = String.format("http://localhost:%s", mockBackend.getPort());
		gitHubService = new GitHubService(new GitHubFacade("clientId", "oauthSecret", baseUrl, baseUrl));
	}

	@Test
	void shouldGetToken() {
		givenGitHubToken();
		var token = gitHubService.getToken("code");
		assertEquals("123", token);
	}

	@Test
	void shouldNotGetToken_whenGitHubTokenIsInvalid() {
		givenGitHubTokenInvalid();
		var ex = assertThrows(ResponseStatusException.class, () -> gitHubService.getToken("code"));
		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	void shouldNotGetToken_whenURIIsInvalid() {
		var failGitHubService = new GitHubService(
				new GitHubFacade("clientId", "oauthSecret", "invalid", "invalid"));

		var ex = assertThrows(SwitcherRemoteException.class, () -> failGitHubService.getToken("code"));
		assertEquals("Something went wrong: It was not possible to reach the Switcher-API on this endpoint: invalid", ex.getMessage());
	}

	@Test
	void shouldGetGitHubDetail() {
		givenGitHubDetails();
		var gitHubDetail = gitHubService.getGitHubDetail("123");
		assertEquals("UserName", gitHubDetail.name());
		assertEquals("login", gitHubDetail.login());
		assertEquals("http://avatar.com", gitHubDetail.avatarUrl());
		assertEquals("123", gitHubDetail.id());
	}

	@Test
	void shouldNotGetGitHubDetail_whenGitHubTokenIsInvalid() {
		givenGitHubTokenInvalid();
		var ex = assertThrows(ResponseStatusException.class, () -> gitHubService.getGitHubDetail("code"));
		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	void shouldNotGetGitHubDetail_whenURIIsInvalid() {
		var failGitHubService = new GitHubService(
				new GitHubFacade("clientId", "oauthSecret", "invalid", "invalid"));

		var ex = assertThrows(SwitcherRemoteException.class, () -> failGitHubService.getGitHubDetail("code"));
		assertEquals("Something went wrong: It was not possible to reach the Switcher-API on this endpoint: invalid", ex.getMessage());
	}

	private void givenGitHubToken() {
		mockBackend.enqueue(new MockResponse()
				.setBody("{\"access_token\":\"123\",\"token_type\":\"bearer\",\"scope\":\"\"}")
				.addHeader("Content-Type", MediaType.APPLICATION_JSON));
	}

	private void givenGitHubDetails() {
		final var githubAccountDetail = new GitHubDetail("123", "UserName", "login", "http://avatar.com");

		try {
			mockBackend.enqueue(new MockResponse()
					.setBody(mapper.writeValueAsString(githubAccountDetail))
					.addHeader("Content-Type", MediaType.APPLICATION_JSON));
		} catch (JsonProcessingException e) {
			log.error("Error on parsing GitHubDetail", e);
		}
	}

	private void givenGitHubTokenInvalid() {
		mockBackend.enqueue(new MockResponse().setResponseCode(400));
	}

}
