package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.PlanService;
import com.github.switcherapi.ac.service.security.JwtTokenService;
import com.github.switcherapi.ac.util.Roles;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureDataMongo
@Execution(ExecutionMode.CONCURRENT)
class AdminAccountControllerTests {

	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;
	@Autowired AccountService accountService;
	@Autowired WebTestClient webTestClient;

	private static final String GITHUB_ID = String.format("mock_github_id_%s", System.currentTimeMillis());
	private static final String ADMIN_ID = "mock_account1";
	private static Admin adminAccount;
	private static Authentication authentication;
	private String bearer;

	@BeforeAll
	static void setup(
			@Autowired AccountService accountService,
			@Autowired AdminService adminService,
			@Autowired ReactiveAuthenticationManager authenticationManage) {
		accountService.createAccount(ADMIN_ID).block();
		adminAccount = adminService.createAdminAccount(GITHUB_ID).block();
		authentication = authenticationManage.authenticate(
				new UsernamePasswordAuthenticationToken(
						Objects.requireNonNull(adminAccount).getId(), GITHUB_ID,
						List.of(new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name())))).block();
	}
	
	@BeforeEach
	void setup(@Autowired AdminService adminService) {
		var plan2 = Plan.loadDefault();
		plan2.setName("BASIC");
		planService.createPlan(plan2).block();

		var token = jwtService.generateToken(authentication).getLeft();
		bearer = String.format("Bearer %s", token);

		StepVerifier.create(adminService.updateAdminAccountToken(adminAccount, token))
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	void testServices() {
		assertNotNull(accountService);
		assertNotNull(planService);
	}

	@Test
	void shouldChangeAccountPlan() {
		// validate before
		var account = accountService.getAccountByAdminId(ADMIN_ID).block();
		var planDefault = planService.getPlanByName(PlanType.DEFAULT.name()).block();
		assertNotNull(account);
		assertNotNull(planDefault);
		assertThat(account.getPlan()).isEqualTo(planDefault.getId());

		// test
		var accountDto = webTestClient.patch()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/account/change/{adminId}")
						.queryParam("plan", "BASIC")
						.build(ADMIN_ID))
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(AccountDTO.class)
				.returnResult()
				.getResponseBody();

		assertNotNull(accountDto);
		assertThat(accountDto.adminId()).isEqualTo(ADMIN_ID);
		assertThat(accountDto.plan().name()).isEqualTo("BASIC");

		account = accountService.getAccountByAdminId(ADMIN_ID).block();
		var planBasic = planService.getPlanByName("BASIC").block();
		assertNotNull(account);
		assertNotNull(planBasic);
		assertThat(account.getPlan()).isEqualTo(planBasic.getId());
	}

	@Test
	void shouldChangeAccountPlan_afterDeletingPlan() {
		// given
		var accountDto = accountService.createAccount(ADMIN_ID, "BASIC").block();

		// validate before
		var planBasic = planService.getPlanByName("BASIC").block();
		assertNotNull(accountDto);
		assertNotNull(planBasic);
		assertThat(accountDto.plan().id()).isEqualTo(planBasic.getId());

		// test
		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/delete")
						.queryParam("plan", "BASIC")
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("Plan deleted");

		var account = accountService.getAccountByAdminId(ADMIN_ID).block();
		var planDefault = planService.getPlanByName(PlanType.DEFAULT.name()).block();
		assertNotNull(account);
		assertNotNull(planDefault);
		assertThat(account.getPlan()).isEqualTo(planDefault.getId());
	}

	@Test
	void shouldNotChangeAccountPlan_invalidAuthorizationKey() {
		this.webTestClient.patch()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/account/change/{adminId}")
						.queryParam("plan", "BASIC")
						.build(ADMIN_ID))
				.header(HttpHeaders.AUTHORIZATION, "Bearer INVALID_KEY")
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotChangeAccountPlan_planNotFound() {
		this.webTestClient.patch()
				.uri(uriBuilder -> uriBuilder.path("/admin/v1/account/change/{adminId}")
						.queryParam("plan", "NOT_FOUND")
						.build(ADMIN_ID))
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound();
	}

}
