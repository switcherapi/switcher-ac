package com.github.switcherapi.ac.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;

@SpringBootTest
@AutoConfigureMockMvc
class ManagementTests {
	
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	
	@Autowired
	MockMvc mockMvc;
	
	private static Admin adminAccount;
	
	private String bearer;
	
	@BeforeAll
	static void setup(
			@Autowired AccountService accountService,
			@Autowired AdminService adminService) {
		accountService.createAccount("mock_account1");
		adminAccount = adminService.createAdminAccount("123456");
	}
	
	@BeforeEach
	void setup() {
		final var token = jwtService.generateToken(adminAccount.getId())[0];
		adminService.updateAdminAccountToken(adminAccount, token);
		bearer = String.format("Bearer %s", token);
	}
	
	@Test
	void shouldNotAccessActuator() throws Exception {
		this.mockMvc.perform(get("/actuator")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_KEY")
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldAccessActuator() throws Exception {
		this.mockMvc.perform(get("/actuator")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
	
	@Test
	void shouldAccessSwagger() throws Exception {
		this.mockMvc.perform(get("/v3/api-docs")
			.contentType(MediaType.APPLICATION_JSON)
			.with(httpBasic("admin", "admin"))
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
	
	@Test
	void shouldNotAccessSwagger() throws Exception {
		this.mockMvc.perform(get("/v3/api-docs")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldAccessSwaggerUI() throws Exception {
		this.mockMvc.perform(get("/swagger-ui/index.html")
			.contentType(MediaType.APPLICATION_JSON)
			.with(httpBasic("admin", "admin"))
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
	
	@Test
	void shouldNotAccessSwaggerUI() throws Exception {
		this.mockMvc.perform(get("/swagger-ui/index.html")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}

}
