package com.switcherapi.ac.controller;

import com.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.domain.PlanAttribute;
import com.switcherapi.ac.model.dto.Metadata;
import com.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.switcherapi.ac.service.AccountService;
import com.switcherapi.ac.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static com.switcherapi.ac.model.domain.Feature.DOMAIN;
import static com.switcherapi.ac.model.domain.Feature.SWITCHER;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureMockMvc
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
			)).build());
	}

	@Test
	void shouldReturnRelayVerificationCode() throws Exception {
		this.mockMvc.perform(get("/switcher/v1/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
						.with(csrf()))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("{\"code\":\"[relay_code]\"}")));
	}
	
	@Test
	void shoutNotCreate_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/switcher/v1/create")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf())
				.content(""))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldCreateAccount() throws Exception {
		//given
		var jsonRequest = givenRequest("adminid");
		var jsonResponse = gson.toJson(ResponseRelayDTO.create(true));

		//test
		this.mockMvc.perform(post("/switcher/v1/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
				.with(csrf())
				.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldRemoveAccount() throws Exception {
		//given
		givenAccount("adminid");
		var jsonRequest = givenRequest("adminid");
		var jsonResponse = gson.toJson(ResponseRelayDTO.create(true));
		
		//test
		this.mockMvc.perform(post("/switcher/v1/remove")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
				.with(csrf())
				.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}

	@Test
	void shouldNotRemoveAccount_accountNotFound() throws Exception {
		//given
		var jsonRequest = givenRequest("NOT_FOUND");
		var jsonResponse = gson.toJson(ResponseRelayDTO.fail("404 NOT_FOUND \"Unable to find account NOT_FOUND\""));
		
		//test
		this.mockMvc.perform(post("/switcher/v1/remove")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
				.with(csrf())
				.content(jsonRequest))
			.andDo(print())
			.andExpect(status().is5xxServerError())
			.andExpect(content().string(containsString(jsonResponse)));
	}

	@Test
	void shouldBeOkWhenValidate_limiter() throws Exception {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = ResponseRelayDTO.success(Metadata.builder().rateLimit(100).build());

		this.assertLimiter("adminid", expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_limiter_accountNotFound() throws Exception {
		var expectedResponse = ResponseRelayDTO.fail("404 NOT_FOUND \"Account not found\"");

		this.assertLimiter("NOT_FOUND", expectedResponse, 404);
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() throws Exception {
		//given
		givenAccount("masteradminid");

		var plan = Plan.loadDefault();
		plan.getFeature(SWITCHER).setValue(-1);
		plan.setName("UNLIMITED");

		planService.createPlan(plan);
		accountService.updateAccountPlan("masteradminid", "UNLIMITED");
		
		//test
		var expectedResponse = ResponseRelayDTO.create(true);
		this.assertValidate("masteradminid", SWITCHER.getValue(),
				10000, expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_payloadMalformed() throws Exception {
		//given
		givenAccount("masteradminid");

		var plan = Plan.loadDefault();
		planService.createPlan(plan);

		//test
		var expectedResponse = ResponseRelayDTO.fail("com.google.gson.stream.MalformedJsonException");
		this.assertValidate500(SWITCHER.getValue(), expectedResponse);
	}

	@Test
	void shouldReturnTrue() throws Exception {
		//given
		givenAccount("adminid_ok", "TEST");

		//test
		var expectedResponse = ResponseRelayDTO.create(true);
		this.assertValidate("adminid_ok", DOMAIN.getValue(),
				0, expectedResponse, 200);
	}

	@Test
	void shouldReturnFalse() throws Exception {
		//given
		givenAccount("adminid_nok", "TEST");

		//test
		var expectedResponse = ResponseRelayDTO.fail("Feature limit has been reached");

		this.assertValidate("adminid_nok", DOMAIN.getValue(),
				1, expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() throws Exception {
		var expectedResponse = ResponseRelayDTO.fail("404 NOT_FOUND \"Account not found\"");

		this.assertValidate("NOT_FOUND", DOMAIN.getValue(),
				0, expectedResponse, 404);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() throws Exception {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = ResponseRelayDTO.fail("400 BAD_REQUEST \"Invalid feature: INVALID_FEATURE\"");

		this.assertValidate("adminid", "INVALID_FEATURE",
				0, expectedResponse, 400);
	}

}
