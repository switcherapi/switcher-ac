package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.AcMockWebServer;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
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
		var token = gitHubService.getToken("code").block();
		assertEquals("123", token);
	}

	@Test
	void shouldNotGetToken_whenGitHubTokenIsInvalid() {
		givenGitHubTokenInvalid();

		var getToken = gitHubService.getToken("code");
		var ex = assertThrows(ResponseStatusException.class, getToken::block);
		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	void shouldNotGetToken_whenURIIsInvalid() {
		var failGitHubService = new GitHubService(
				new GitHubFacade("clientId", "oauthSecret", "invalid", "invalid"));

		var getToken = failGitHubService.getToken("code");
		var ex = assertThrows(ResponseStatusException.class, getToken::block);
		assertEquals("401 UNAUTHORIZED \"Invalid GitHub account\"", ex.getMessage());
	}

	@Test
	void shouldGetGitHubDetail() {
		givenGitHubDetails();
		var gitHubDetail = gitHubService.getGitHubDetail("123").block();
		assertNotNull(gitHubDetail);
		assertEquals("UserName", gitHubDetail.name());
		assertEquals("login", gitHubDetail.login());
		assertEquals("http://avatar.com", gitHubDetail.avatarUrl());
		assertEquals("123", gitHubDetail.id());
	}

	@Test
	void shouldNotGetGitHubDetail_whenGitHubTokenIsInvalid() {
		givenGitHubTokenInvalid();

		var getGitHubDetail = gitHubService.getGitHubDetail("code");
		var ex = assertThrows(ResponseStatusException.class, getGitHubDetail::block);
		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	void shouldNotGetGitHubDetail_whenURIIsInvalid() {
		var failGitHubService = new GitHubService(
				new GitHubFacade("clientId", "oauthSecret", "invalid", "invalid"));

		var getGitHubDetail = failGitHubService.getGitHubDetail("code");
		var ex = assertThrows(ResponseStatusException.class, getGitHubDetail::block);
		assertEquals("401 UNAUTHORIZED \"Invalid GitHub account\"", ex.getMessage());
	}

}
