package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.ws.rs.core.HttpHeaders;

import com.github.switcherapi.ac.model.domain.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.ac.service.PlanService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminAccountControllerTests {
	
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;
	@Autowired AccountService accountService;
	@Autowired ValidatorFactory validatorFactory;
	
	@Autowired MockMvc mockMvc;
	
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
		final PlanV2 plan2 = PlanV2.loadDefault();
		plan2.setName("BASIC");
		planService.createPlanV2(plan2);
		
		final var token = jwtService.generateToken(adminAccount.getId())[0];
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
		Account account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getPlanV2().getName()).isEqualTo(PlanType.DEFAULT.name());
		
		//test
		var json = this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		
		var accountDto = new ObjectMapper().readValue(json, AccountDTO.class);
		assertThat(accountDto.getAdminId()).isEqualTo("mock_account1");
		assertThat(accountDto.getPlan().getName()).isEqualTo("BASIC");
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getPlanV2().getName()).isEqualTo("BASIC");
	}
	
	@Test
	void shouldChangeAccountPlan_afterDeletingPlan() throws Exception {
		//given
		Account account = accountService.createAccount("mock_account1", "BASIC");
		
		//validate before
		assertThat(account.getPlanV2().getName()).isEqualTo("BASIC");
		
		//test
		this.mockMvc.perform(delete("/plan/v2/delete")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andExpect(status().isOk())
			.andExpect(content().string("Plan deleted"));
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getPlanV2().getName()).isEqualTo(PlanType.DEFAULT.name());
	}
	
	@Test
	void shouldNotChangeAccountPlan_invalidAuthorizationKey() throws Exception {
		this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_KEY")
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotChangeAccountPlan_planNotFound() throws Exception {
		this.mockMvc.perform(patch("/admin/v1/account/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
	@Test
	void shouldResetDailyExecution() throws Exception {
		//given
		var request = FeaturePayload.builder()
				.feature("daily_execution")
				.owner("mock_account1")
				.build();
		validatorFactory.runValidator(request);
		
		//validate before
		Account account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getCurrentDailyExecution()).isEqualTo(1);
		
		//test
		this.mockMvc.perform(patch("/admin/v1/account/reset/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getCurrentDailyExecution()).isZero();
	}

}
