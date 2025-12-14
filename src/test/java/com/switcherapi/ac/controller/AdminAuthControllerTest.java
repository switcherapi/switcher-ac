package com.switcherapi.ac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.switcherapi.ac.repository.AdminRepository;
import com.switcherapi.ac.service.AdminService;
import com.switcherapi.ac.service.JwtTokenService;
import com.switcherapi.client.test.SwitcherTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminAuthControllerTest {
	
	@Autowired AdminRepository adminRepository;
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired MockMvc mockMvc;

	private static final String GITHUB_ID = String.format("[AdminAuthControllerTests]_github_id_%s", System.currentTimeMillis());
	private Pair<String, String> tokens;
	
	@BeforeEach
	void setup() {
		final var admin = adminService.createAdminAccount(GITHUB_ID);
		tokens = jwtService.generateToken(admin.getId());
		adminService.updateAdminAccountToken(admin, tokens.getLeft());
	}
	
	@Test
	void shouldRefreshToken() throws Exception {
		var count = new CountDownLatch(1);
		assertFalse(count.await(1, TimeUnit.SECONDS));
		
		var json = this.mockMvc.perform(post("/admin/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.with(csrf())
				.queryParam("refreshToken", tokens.getRight()))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		var authDto = new ObjectMapper().readValue(json, GitHubAuthDTO.class);
		assertThat(authDto.admin().gitHubId()).isEqualTo(GITHUB_ID);
		assertThat(authDto.token()).isNotEqualTo(tokens.getLeft());
		assertThat(authDto.refreshToken()).isNotEqualTo(tokens.getRight());
	}

	@Test
	void shouldNotRefreshToken_invalidToken() throws Exception {
		this.mockMvc.perform(post("/admin/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_TOKEN")
				.with(csrf())
				.queryParam("refreshToken", tokens.getRight()))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldNotRefreshToken_missingToken() throws Exception {
		this.mockMvc.perform(post("/admin/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.with(csrf())
						.queryParam("refreshToken", tokens.getRight()))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}

	@SwitcherTest(key = "SWITCHER_AC_ADM", result = false)
	void shouldNotRefreshToken_accountUnauthorized() throws Exception {
		this.mockMvc.perform(post("/admin/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
						.with(csrf())
						.queryParam("refreshToken", tokens.getRight()))
				.andDo(print())
				.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotRefreshToken_invalidRefreshToken() throws Exception {
		this.mockMvc.perform(post("/admin/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.with(csrf())
				.queryParam("refreshToken", "INVALID_REFRESH_TOKEN"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldLogout() throws Exception {
		this.mockMvc.perform(post("/admin/v1/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.getLeft())
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
		
		var admin = adminRepository.findByGitHubId(GITHUB_ID);
		assertThat(admin.getToken()).isNull();
	}
	
}
