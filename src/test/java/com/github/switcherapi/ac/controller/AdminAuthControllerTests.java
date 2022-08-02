package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.repository.AdminRepository;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminAuthControllerTests {
	
	@Autowired AdminRepository adminRepository;
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	
	@Autowired
	MockMvc mockMvc;
	
	private String[] tokens;
	
	@BeforeEach
	void setup() {
		final Admin admin = adminService.createAdminAccount("123456");
		tokens = jwtService.generateToken(admin.getId());
		adminService.updateAdminAccountToken(admin, tokens[0]);
	}
	
	@Test
	void shouldRefreshToken() throws Exception {
		CountDownLatch count = new CountDownLatch(1);
		count.await(1, TimeUnit.SECONDS);
		
		var json = this.mockMvc.perform(post("/admin/v1/auth/refresh")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens[0])
			.with(csrf())
			.queryParam("refreshToken", tokens[1]))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		var authDto = new ObjectMapper().readValue(json, GitHubAuthDTO.class);
		assertThat(authDto.getAdmin().getGitHubId()).isEqualTo("123456");
		assertThat(authDto.getToken()).isNotEqualTo(tokens[0]);
		assertThat(authDto.getRefreshToken()).isNotEqualTo(tokens[1]);
	}
	
	@Test
	void shouldNotRefreshToken_invalidRefreshToken() throws Exception {
		this.mockMvc.perform(post("/admin/v1/auth/refresh")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens[0])
			.with(csrf())
			.queryParam("refreshToken", "INVALID_REFRESH_TOKEN"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldLogout() throws Exception {
		this.mockMvc.perform(post("/admin/v1/logout")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens[0])
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
		
		Admin admin = adminRepository.findByGitHubId("123456");
		assertThat(admin.getToken()).isNull();
	}
	
}
