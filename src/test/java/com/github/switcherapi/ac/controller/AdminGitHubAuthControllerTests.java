package com.github.switcherapi.ac.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.config.SwitcherFeatures;
import com.github.switcherapi.ac.model.GitHubDetail;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import com.github.switcherapi.client.test.SwitcherTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class AdminGitHubAuthControllerTests {

	@Autowired WebTestClient webTestClient;
	@Autowired ApplicationContext applicationContext;
	
	public static MockWebServer mockBackend;
	private final ObjectMapper mapper = new ObjectMapper();

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
		final GitHubFacade gitHubFacade = new GitHubFacade("clientId", "oauthSecret", baseUrl, baseUrl);

		final DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		beanFactory.destroySingleton("gitHubFacade");
		beanFactory.registerSingleton("gitHubFacade", gitHubFacade);
	}
	
	@Test
	void testSwitchers() {
		assertDoesNotThrow(SwitcherFeatures::checkSwitchers);
	}

	@SwitcherTest(key = SwitcherFeatures.SWITCHER_AC_ADM)
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldLoginWithGitHub() throws Exception {
		// given
		givenGitHubToken();
		givenResponseSuccess();

		// test
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(GitHubAuthDTO.class)
				.value(authDto -> {
					assertThat(authDto.admin()).isNotNull();
					assertThat(authDto.token()).isNotNull();
					assertThat(authDto.refreshToken()).isNotNull();
				});
	}

	@SwitcherTest(key = SwitcherFeatures.SWITCHER_AC_ADM, result = false)
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldNotLoginWithGitHub_accountNotAllowed() throws Exception {
		//given
		givenGitHubToken();
		givenResponseSuccess();

		//test
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidToken() {
		//given
		givenGitHubToken();
		givenResponse401();

		//test
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotGetGitHubToken_serviceUnavailable() {
		//given
		givenResponse503();

		//test
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@SwitcherTest(key = SwitcherFeatures.SWITCHER_AC_ADM, result = false)
	@Execution(ExecutionMode.SAME_THREAD)
	void shouldNotLoginWithGitHub_invalidCode() {
		//given
		givenResponseInvalidCode();

		//test
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidUrl() {
		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/admin/auth/github")
						.queryParam("code", "123")
						.build())
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	private void givenGitHubToken() {
		mockBackend.enqueue(new MockResponse()
				.setBody("{\"access_token\":\"123\",\"token_type\":\"bearer\",\"scope\":\"\"}")
				.addHeader("Content-Type", MediaType.APPLICATION_JSON));
	}

	private void givenResponse401() {
		mockBackend.enqueue(new MockResponse().setResponseCode(
				HttpURLConnection.HTTP_UNAUTHORIZED));
	}

	private void givenResponse503() {
		mockBackend.enqueue(new MockResponse().setResponseCode(
				HttpURLConnection.HTTP_UNAVAILABLE));
	}

	private void givenResponseInvalidCode() {
		mockBackend.enqueue(new MockResponse()
				.setBody("{ \"error\": \"Invalid code\" }")
				.addHeader("Content-Type", MediaType.APPLICATION_JSON));
	}

	private void givenResponseSuccess() throws JsonProcessingException {
		final var githubAccountDetail = new GitHubDetail("123", "UserName", "login", "http://avatar.com");

		mockBackend.enqueue(new MockResponse()
				.setBody(mapper.writeValueAsString(githubAccountDetail))
				.addHeader("Content-Type", MediaType.APPLICATION_JSON));
	}

}
