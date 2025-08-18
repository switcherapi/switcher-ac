package com.switcherapi.ac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switcherapi.ac.AcMockWebServer;
import com.switcherapi.ac.config.SwitcherFeatures;
import com.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.switcherapi.ac.service.facades.GitHubFacade;
import com.switcherapi.client.test.SwitcherTest;
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
class AdminGitHubAuthControllerTest extends AcMockWebServer {

	@Autowired MockMvc mockMvc;
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

}
