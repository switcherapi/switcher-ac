package com.github.switcherapi.ac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.ac.service.PlanService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminAccountControllerTests {
	
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;
	@Autowired AccountService accountService;
	@Autowired MockMvc mockMvc;

	private static final String ADMIN_ID = "mock_account1";
	private static Admin adminAccount;
	private String bearer;

	@BeforeAll
	static void setup(
			@Autowired AccountService accountService,
			@Autowired AdminService adminService) {
		accountService.createAccount(ADMIN_ID);
		adminAccount = adminService.createAdminAccount("123456");
	}
	
	@BeforeEach
	void setup() {
		final var plan2 = Plan.loadDefault();
		plan2.setName("BASIC");
		planService.createPlan(plan2);
		
		final var token = jwtService.generateToken(adminAccount.getId()).getLeft();
		adminService.updateAdminAccountToken(adminAccount, token);
		bearer = String.format("Bearer %s", token);
	}

	@Test
	void testServices() {
		assertThat(accountService).isNotNull();
		assertThat(planService).isNotNull();
	}
	
	@Test
	void shouldChangeAccountPlan() throws Exception {
		//validate before
		var account = accountService.getAccountByAdminId(ADMIN_ID);
		assertThat(account.getPlan().getName()).isEqualTo(PlanType.DEFAULT.name());
		
		//test
		var json = this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", ADMIN_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.with(csrf())
				.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		var accountDto = new ObjectMapper().readValue(json, AccountDTO.class);
		assertThat(accountDto.adminId()).isEqualTo(ADMIN_ID);
		assertThat(accountDto.plan().name()).isEqualTo("BASIC");
		
		account = accountService.getAccountByAdminId(ADMIN_ID);
		assertThat(account.getPlan().getName()).isEqualTo("BASIC");
	}
	
	@Test
	void shouldChangeAccountPlan_afterDeletingPlan() throws Exception {
		//given
		var account = accountService.createAccount(ADMIN_ID, "BASIC");
		
		//validate before
		assertThat(account.getPlan().getName()).isEqualTo("BASIC");
		
		//test
		this.mockMvc.perform(delete("/plan/v2/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.with(csrf())
				.queryParam("plan", "BASIC"))
			.andExpect(status().isOk())
			.andExpect(content().string("Plan deleted"));
		
		account = accountService.getAccountByAdminId(ADMIN_ID);
		assertThat(account.getPlan().getName()).isEqualTo(PlanType.DEFAULT.name());
	}
	
	@Test
	void shouldNotChangeAccountPlan_invalidAuthorizationKey() throws Exception {
		this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", ADMIN_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_KEY")
				.with(csrf())
				.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotChangeAccountPlan_planNotFound() throws Exception {
		this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", ADMIN_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.with(csrf())
				.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

}
