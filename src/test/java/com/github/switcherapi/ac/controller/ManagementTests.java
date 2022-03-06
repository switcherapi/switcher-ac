package com.github.switcherapi.ac.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.switcherapi.ac.model.Admin;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;

@SpringBootTest
@AutoConfigureMockMvc
class ManagementTests {
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private JwtTokenService jwtService;
	
	@Autowired
	private MockMvc mockMvc;
	
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
			.header("Authorization", "Bearer INVALID_KEY")
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldAccessActuator() throws Exception {
		this.mockMvc.perform(get("/actuator")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", bearer)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
	
	@Test
	void shouldAccessSwagger() throws Exception {
		this.mockMvc.perform(get("/v2/api-docs")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}
	
	@Test
	void shouldAccessSwaggerUI() throws Exception {
		this.mockMvc.perform(get("/swagger-ui/")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
	}

}
