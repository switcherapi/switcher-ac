package com.github.switcherapi.ac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.repository.AdminRepository;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.client.test.SwitcherTest;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class AdminAuthControllerTests {
	
	@Autowired AdminRepository adminRepository;
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired MockMvc mockMvc;
	
	private Pair<String, String> tokens;
	
	@BeforeEach
	void setup() {
		final var admin = adminService.createAdminAccount("123456");
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
		assertThat(authDto.getAdmin().getGitHubId()).isEqualTo("123456");
		assertThat(authDto.getToken()).isNotEqualTo(tokens.getLeft());
		assertThat(authDto.getRefreshToken()).isNotEqualTo(tokens.getRight());
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
		var count = new CountDownLatch(1);
		assertFalse(count.await(1, TimeUnit.SECONDS));

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
		
		var admin = adminRepository.findByGitHubId("123456");
		assertThat(admin.getToken()).isNull();
	}
	
}
