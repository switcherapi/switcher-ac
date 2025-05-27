package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static com.github.switcherapi.ac.model.domain.Feature.HISTORY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class PlanControllerTests extends ControllerTestUtils {

	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;

	private static final String GITHUB_ID = String.format("mock_github_id_%s", System.currentTimeMillis());
	private static Admin adminAccount;
	private static Authentication authentication;
	private String bearer;

	@BeforeAll
	static void setup(@Autowired AdminService adminService, @Autowired ReactiveAuthenticationManager authenticationManage) {
		adminAccount = adminService.createAdminAccount(GITHUB_ID).block();
		authentication = authenticationManage.authenticate(
				new UsernamePasswordAuthenticationToken(
						Objects.requireNonNull(adminAccount).getId(), GITHUB_ID,
						List.of(new SimpleGrantedAuthority(Roles.ROLE_ADMIN.name())))).block();
	}

	@BeforeEach
	void setup(@Autowired AdminService adminService) {
		var token = jwtService.generateToken(authentication).getLeft();
		bearer = String.format("Bearer %s", token);

		StepVerifier.create(adminService.updateAdminAccountToken(adminAccount, token))
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	void testPlanService() {
		assertNotNull(planService);
	}

	@Test
	void shoutNotDelete_notAuthenticated() {
		webTestClient.delete()
				.uri("/plan/v2/delete?plan=BASIC")
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldCreateNewPlan() throws Exception {
		//given
		var planObj = Plan.loadDefault();
		var json = gson.toJson(planObj);

		//test
		var response = webTestClient.post()
				.uri("/plan/v2/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.bodyValue(json)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult().getResponseBody();

		assertDtoResponse(planObj, response);
	}

	@Test
	void shouldUpdatePlan() throws Exception {
		//given
		var planObj = Plan.loadDefault();
		planObj.setName(PlanType.DEFAULT.name());
		planObj.getFeature(HISTORY).setValue(true);
		var json = gson.toJson(planObj);

		//test
		final var old = planService.getPlanByName(PlanType.DEFAULT.name()).block();
		assertNotNull(old);
		assertEquals(false, old.getFeature(HISTORY).getValue());

		var response = webTestClient.patch()
				.uri("/plan/v2/update")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.bodyValue(json)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult().getResponseBody();

		assertDtoResponse(planObj, response);
		final var planUpdated = planService.getPlanByName(PlanType.DEFAULT.name()).block();
		assertNotNull(planUpdated);
		assertEquals(true, planUpdated.getFeature(HISTORY).getValue());
	}

	@Test
	void shouldNotUpdatePlan_planNotFound() {
		//given
		var planObj = Plan.loadDefault();
		planObj.setName("NOT_FOUND");
		var json = gson.toJson(planObj);

		//test
		webTestClient.patch()
				.uri("/plan/v2/update")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.bodyValue(json)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldDeletePlan() {
		//given
		final var planObj = Plan.loadDefault();
		planObj.setName("DELETE_ME");
		var json = gson.toJson(planObj);

		webTestClient.post()
				.uri("/plan/v2/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.bodyValue(json)
				.exchange()
				.expectStatus().isOk();

		//test
		final var planBeforeDelete = planService.getPlanByName("DELETE_ME").block();
		assertNotNull(planBeforeDelete);

		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/delete")
						.queryParam("plan", "DELETE_ME")
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("Plan deleted");

		var planNotFound = planService.getPlanByName("DELETE_ME");
		assertThrows(ResponseStatusException.class, planNotFound::block, "Unable to find plan DELETE_ME");
	}

	@Test
	void shouldNotDeletePlan_planCannotBeDeleted() {
		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/delete")
						.queryParam("plan", PlanType.DEFAULT.name())
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().is4xxClientError()
				.expectBody(String.class)
				.consumeWith(response -> {
					var errorMessage = response.getResponseBody();
					assertNotNull(errorMessage);
					assertTrue(errorMessage.contains("Invalid plan name"));
				});
	}

	@Test
	void shouldNotDeletePlan_planNotFound() {
		webTestClient.delete()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/delete")
						.queryParam("plan", "NOT_FOUND")
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void shouldListPlans() {
		//given
		final var plans = planService.listAll();
		assertNotNull(plans);

		//test
		webTestClient.get()
				.uri("/plan/v2/list")
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Plan.class)
				.consumeWith(response -> {
					var responseBody = response.getResponseBody();
					assertNotNull(responseBody);
					assertTrue(responseBody.stream().anyMatch(plan -> plan.getName().equals(PlanType.DEFAULT.name())));
				});
	}

	@Test
	void shouldGetPlanByName() throws Exception {
		//given
		final var plan = planService.getPlanByName(PlanType.DEFAULT.name()).block();
		assertNotNull(plan);

		//test
		var response = webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/get")
						.queryParam("plan", PlanType.DEFAULT.name())
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.returnResult().getResponseBody();

		assertDtoResponse(plan, response);
	}

	@Test
	void shouldNotGetPlanByName_plaNotFound() {
		webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/plan/v2/get")
						.queryParam("plan", "NOT_FOUND")
						.build())
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.exchange()
				.expectStatus().isNotFound();
	}

}
