package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.Admin;
import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.model.request.RequestRelay;
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
	
	private String token;
	
	@BeforeEach
	void setup() {
		final PlanDTO plan1 = Plan.loadDefault();
		planService.createPlan(plan1);
		
		final PlanDTO plan2 = Plan.loadDefault();
		plan2.setName("BASIC");
		planService.createPlan(plan2);
		
		accountService.createAccount("mock_account1");
		
		final Admin admin = adminService.createAdminAccount("123456");
		token = jwtService.generateToken(admin.getId())[0];
		adminService.updateAdminAccountToken(admin,token);
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
		this.mockMvc.perform(patch("/admin/account/v1/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isOk());
		
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
		this.mockMvc.perform(delete("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andExpect(status().isOk());
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getPlan().getName()).isEqualTo(PlanType.DEFAULT.name());
	}
	
	@Test
	void shouldNotChangeAccountPlan_invalidAuthorizationKey() throws Exception {
		this.mockMvc.perform(patch("/admin/account/v1/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer INVALID_KEY")
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andDo(print())
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldNotChangeAccountPlan_planNotFound() throws Exception {
		this.mockMvc.perform(patch("/admin/account/v1/change/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
	@Test
	void shouldResetDailyExecution() throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setValue("execution#mock_account1");
		validatorFactory.runValidator(request);
		
		//validate before
		Account account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getCurrentDailyExecution()).isEqualTo(1);
		
		//test
		this.mockMvc.perform(patch("/admin/account/v1/reset/{adminId}", "mock_account1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk());
		
		account = accountService.getAccountByAdminId("mock_account1");
		assertThat(account.getCurrentDailyExecution()).isZero();
	}

}
