package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.AcMockWebServer;
import com.github.switcherapi.ac.config.SwitcherFeatures;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import com.switcherapi.client.test.SwitcherTest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class AdminGitHubAuthControllerTest extends AcMockWebServer {

	@Autowired WebTestClient webTestClient;
	@Autowired ApplicationContext applicationContext;
	
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

}
