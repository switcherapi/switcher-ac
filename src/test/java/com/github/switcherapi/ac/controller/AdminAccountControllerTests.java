package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.ws.rs.core.HttpHeaders;

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
import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
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
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private JwtTokenService jwtService;
	
	@Autowired
	private PlanService planService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Autowired
	private MockMvc mockMvc;
	
	private static Admin adminAccount;
	
	private String bearer;
	
	@BeforeAll
	static void setup(
			@Autowired AccountService accountService,
			@Autowired AdminService adminService,
			@Autowired PlanService planService) {
		final Plan plan1 = Plan.loadDefault();
		planService.createPlan(plan1);
		
		accountService.createAccount("mock_account1");
		adminAccount = adminService.createAdminAccount("123456");
	}
	
	@BeforeEach
	void setup() {
		final Plan plan2 = Plan.loadDefault();
		plan2.setName("BASIC");
		planService.createPlan(plan2);
		
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
		assertThat(account.getPlan().getName()).isEqualTo(PlanType.DEFAULT.name());
		
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
		assertThat(account.getPlan().getName()).isEqualTo("BASIC");
	}
	
	@Test
	void shouldChangeAccountPlan_afterDeletingPlan() throws Exception {
		//given
		Account account = accountService.createAccount("mock_account1", "BASIC");
		
		//validate before
		assertThat(account.getPlan().getName()).isEqualTo("BASIC");
		
		//test
		this.mockMvc.perform(delete("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andExpect(status().isOk())
			.andExpect(content().string("Plan deleted"));
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getPlan().getName()).isEqualTo(PlanType.DEFAULT.name());
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
		FeaturePayload request = new FeaturePayload();
		request.setFeature("execution");
		request.setOwner("mock_account1");
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
