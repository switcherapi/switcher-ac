package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.dto.Metadata;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.model.domain.Feature.DOMAIN;
import static com.github.switcherapi.ac.model.domain.Feature.SWITCHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class SwitcherRelayControllerTest extends ControllerTestUtils {
	
	@Autowired AccountService accountService;
	@Autowired PlanService planService;

	@BeforeEach
	void setupPlan() {
		planService.createPlan(Plan.builder()
			.name("TEST")
			.attributes(List.of(
				PlanAttribute.builder().feature(DOMAIN.getValue()).value(1).build()
			)).build()).block();
	}

	@Test
	void shouldReturnRelayVerificationCode() {
		webTestClient.get()
				.uri("/switcher/v1/verify")
				.headers(httpHeaders -> httpHeaders.setBearerAuth("relay_token"))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.code").isEqualTo("[relay_code]");
	}

	@Test
	void shoutNotCreate_notAuthenticated() {
		webTestClient.post()
				.uri("/switcher/v1/create")
				.contentType(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldCreateAccount() {
		//given
		var jsonRequest = givenRequest("adminid");
		var jsonResponse = gson.toJson(ResponseRelayDTO.create(true));

		//test
		webTestClient.post()
				.uri("/switcher/v1/create")
				.headers(httpHeaders -> httpHeaders.setBearerAuth("relay_token"))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(jsonRequest)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.json(jsonResponse);
	}

	@Test
	void shouldRemoveAccount() {
		//given
		givenAccount("adminid");
		var jsonRequest = givenRequest("adminid");
		var jsonResponse = gson.toJson(ResponseRelayDTO.create(true));

		//test
		webTestClient.post()
				.uri("/switcher/v1/remove")
				.headers(httpHeaders -> httpHeaders.setBearerAuth("relay_token"))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(jsonRequest)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.json(jsonResponse);

		// assert
		var accountServiceMono = accountService.getAccountByAdminId("adminid");
		var ex = assertThrows(ResponseStatusException.class, accountServiceMono::block);
		assertEquals("Unable to find account adminid", ex.getReason());
	}

	@Test
	void shouldNotRemoveAccount_invalidRelayToken() {
		//given
		givenAccount("adminid");
		var jsonRequest = givenRequest("adminid");

		//test
		webTestClient.post()
				.uri("/switcher/v1/remove")
				.headers(httpHeaders -> httpHeaders.setBearerAuth("invalid_relay_token"))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(jsonRequest)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void shouldNotRemoveAccount_accountNotFound() {
		//given
		var jsonRequest = givenRequest("NOT_FOUND");
		var jsonResponse = gson.toJson(ResponseRelayDTO.fail("404 NOT_FOUND \"Unable to find account NOT_FOUND\""));

		//test
		webTestClient.post()
				.uri("/switcher/v1/remove")
				.headers(httpHeaders -> httpHeaders.setBearerAuth("relay_token"))
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(jsonRequest)
				.exchange()
				.expectStatus().is5xxServerError()
				.expectBody()
				.json(jsonResponse);

		var accountServiceMono = accountService.getAccountByAdminId("NOT_FOUND");
		var ex = assertThrows(ResponseStatusException.class, accountServiceMono::block);
		assertEquals("Unable to find account NOT_FOUND", ex.getReason());
	}

	@Test
	void shouldBeOkWhenValidate_limiter() {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = ResponseRelayDTO.success(Metadata.builder().rateLimit(100).build());

		this.assertLimiter("adminid", expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_limiter_accountNotFound() {
		var expectedResponse = ResponseRelayDTO.fail("404 NOT_FOUND \"Account not found\"");

		this.assertLimiter("NOT_FOUND", expectedResponse, 404);
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() {
		//given
		givenAccount("masteradminid");

		var plan = Plan.loadDefault();
		plan.getFeature(SWITCHER).setValue(-1);
		plan.setName("UNLIMITED");

		planService.createPlan(plan).block();
		accountService.updateAccountPlan("masteradminid", "UNLIMITED").block();
		
		//test
		var expectedResponse = ResponseRelayDTO.create(true);
		this.assertValidate("masteradminid", SWITCHER.getValue(),
				10000, expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_payloadMalformed() {
		//given
		givenAccount("masteradminid");

		var plan = Plan.loadDefault();
		planService.createPlan(plan).block();

		//test
		var expectedResponse = ResponseRelayDTO.fail("com.google.gson.stream.MalformedJsonException");
		this.assertValidate500(SWITCHER.getValue(), expectedResponse);
	}

	@Test
	void shouldReturnTrue() {
		//given
		givenAccount("adminid_ok", "TEST");

		//test
		var expectedResponse = ResponseRelayDTO.create(true);
		this.assertValidate("adminid_ok", DOMAIN.getValue(),
				0, expectedResponse, 200);
	}

	@Test
	void shouldReturnFalse() {
		//given
		givenAccount("adminid_nok", "TEST");

		//test
		var expectedResponse = ResponseRelayDTO.fail("Feature limit has been reached");

		this.assertValidate("adminid_nok", DOMAIN.getValue(),
				1, expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() {
		var expectedResponse = ResponseRelayDTO.fail("404 NOT_FOUND \"Account not found\"");

		this.assertValidate("NOT_FOUND", DOMAIN.getValue(),
				0, expectedResponse, 404);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = ResponseRelayDTO.fail("400 BAD_REQUEST \"Invalid feature: INVALID_FEATURE\"");

		this.assertValidate("adminid", "INVALID_FEATURE",
				0, expectedResponse, 400);
	}

}
