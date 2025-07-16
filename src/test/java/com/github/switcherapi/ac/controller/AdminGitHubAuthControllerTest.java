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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminGitHubAuthControllerTests {

	@Autowired MockMvc mockMvc;
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
	void shouldLoginWithGitHub() throws Exception {
		//given
		givenGitHubToken();
		givenResponseSuccess();
		
		//test
		var json = this.mockMvc.perform(post("/admin/v1/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("admin")))
			.andExpect(content().string(containsString("token")))
			.andReturn().getResponse().getContentAsString();
		
		var authDto = new ObjectMapper().readValue(json, GitHubAuthDTO.class);
		assertThat(authDto.admin()).isNotNull();
		assertThat(authDto.token()).isNotNull();
		assertThat(authDto.refreshToken()).isNotNull();
	}

	@SwitcherTest(key = SwitcherFeatures.SWITCHER_AC_ADM, result = false)
	void shouldNotLoginWithGitHub_accountNotAllowed() throws Exception {
		//given
		givenGitHubToken();
		givenResponseSuccess();

		//test
		this.mockMvc.perform(post("/admin/v1/auth/github")
						.contentType(MediaType.APPLICATION_JSON)
						.queryParam("code", "123"))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidToken() throws Exception {
		//given
		givenGitHubToken();
		givenResponse401();

		//test
		this.mockMvc.perform(post("/admin/v1/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldNotGetGitHubToken_serviceUnavailable() throws Exception {
		//given
		givenResponse503();

		//test
		this.mockMvc.perform(post("/admin/v1/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@SwitcherTest(key = SwitcherFeatures.SWITCHER_AC_ADM, result = false)
	void shouldNotLoginWithGitHub_invalidCode() throws Exception {
		//given
		givenResponseInvalidCode();

		//test
		this.mockMvc.perform(post("/admin/v1/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidUrl() throws Exception {
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
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
