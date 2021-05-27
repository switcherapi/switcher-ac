package com.github.switcherapi.ac.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.config.SwitcherFeatures;
import com.github.switcherapi.ac.model.response.GitHubDetailResponse;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.GitHubService;
import com.github.switcherapi.ac.service.facades.GitHubFacade;
import com.github.switcherapi.client.configuration.SwitcherMock;
import com.github.switcherapi.client.factory.SwitcherExecutor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminGitHubAuthControllerTests {
	
	public static MockWebServer mockBackend;
	
	private ObjectMapper MAPPER = new ObjectMapper();
	
	private GitHubService gitHubService;

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ApplicationContext context;

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
	    String baseUrl = String.format("http://localhost:%s", mockBackend.getPort());
	    gitHubService = new GitHubService(new GitHubFacade(baseUrl, baseUrl));
		context.getBean(AdminService.class).setGithubService(gitHubService);
	}
	
	@Test
	void testSwitchers() {
		assertDoesNotThrow(() -> SwitcherFeatures.checkSwitchers());
	}

	@SwitcherMock(key = SwitcherFeatures.SWITCHER_AC_ADM, result = true)
	@ParameterizedTest
	void shouldLoginWithGitHub() throws Exception {
		//given
		mockGitHub();
		
		//test
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
		.andDo(print())
		.andExpect(status().isOk())
		.andExpect(content().string(containsString("admin")))
		.andExpect(content().string(containsString("token")));
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidToken() throws Exception {
		//given
		mockBackend.enqueue(new MockResponse()
				.setBody("{ \"access_token\": \"123\" }")
				.addHeader("Content-Type", "application/json"));
		
		mockBackend.enqueue(new MockResponse().setResponseCode(401));
		
		//test
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotGetGitHubToken_serviceUnavailable() throws Exception {
		//given
		mockBackend.enqueue(new MockResponse().setHttp2ErrorCode(
				HttpURLConnection.HTTP_UNAVAILABLE));
		
		//test
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotLoginWithGitHub_invalidCode() throws Exception {
		//given
		mockBackend.enqueue(new MockResponse()
				.setBody("{ \"error\": \"Invalid code\" }")
				.addHeader("Content-Type", "application/json"));
		
		//test
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotLoginWithGitHub_notAvailable() throws Exception {
		//given
		mockGitHub();
		SwitcherExecutor.assume("SWITCHER_AC_ADM", false);
		
		//test
		this.mockMvc.perform(post("/admin/auth/github")
				.contentType(MediaType.APPLICATION_JSON)
				.queryParam("code", "123"))
		.andDo(print())
		.andExpect(status().isUnauthorized());
	}
	
	void mockGitHub() throws JsonProcessingException {
		mockBackend.enqueue(new MockResponse()
				.setBody("{ \"access_token\": \"123\" }")
				.addHeader("Content-Type", "application/json"));
		
		GitHubDetailResponse githubAccountDetail = new GitHubDetailResponse();
		githubAccountDetail.setId("123");
		githubAccountDetail.setLogin("login");
		githubAccountDetail.setName("UserName");
		
		mockBackend.enqueue(new MockResponse()
			.setBody(MAPPER.writeValueAsString(githubAccountDetail))
			.addHeader("Content-Type", "application/json"));
	}

}
