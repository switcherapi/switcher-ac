package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.AcMockWebServer;
import com.github.switcherapi.ac.exception.SwitcherAcException;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class GitHubServiceTest extends AcMockWebServer {

	private GitHubService gitHubService;

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

		var ex = assertThrows(SwitcherAcException.class, () -> failGitHubService.getToken("code"));
		assertEquals("Something went wrong trying with url: invalid", ex.getMessage());
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

		var ex = assertThrows(SwitcherAcException.class, () -> failGitHubService.getGitHubDetail("code"));
		assertEquals("Something went wrong trying with url: invalid", ex.getMessage());
	}

}
