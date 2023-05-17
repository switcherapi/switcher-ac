package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;
import com.github.switcherapi.ac.service.validator.beans.ValidateRateLimit;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class SwitcherRelayControllerTests extends ControllerTestUtils {
	
	@Autowired AccountService accountService;
	@Autowired PlanService planService;

	@BeforeEach
	void setupPlan() {
		planService.createPlan(Plan.builder()
			.name("TEST")
			.attributes(List.of(
				PlanAttribute.builder().feature("feature_integer").value(1).build()
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
		var jsonResponse = givenTrueAsExpectedResponse();
		
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
		var jsonResponse = givenTrueAsExpectedResponse();
		
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
		var jsonResponse = givenExpectedResponse(false,
				"404 NOT_FOUND \"Unable to find account NOT_FOUND\"");
		
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
	void shouldBeOkWhenValidate_execution() throws Exception {
		//given
		givenAccount("adminid");
		
		//test
		var expectedResponse = new ResponseRelayDTO(true);
		this.assertExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution() throws Exception {
		//given
		givenAccount("adminid");
		var plan = Plan.loadDefault();
		plan.getFeature("daily_execution").setValue(0);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);

		//test
		var expectedResponse = new ResponseRelayDTO(false,"Daily execution limit has been reached");
		this.assertExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution_accountNotFound() throws Exception {
		var expectedResponse = new ResponseRelayDTO(false, "404 NOT_FOUND \"Account not found\"");
		this.assertExecution("NOT_FOUND", expectedResponse, 404);
	}

	@Test
	void shouldBeOkWhenValidate_limiter() throws Exception {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = new ResponseRelayDTO(true, String.format(ValidateRateLimit.MESSAGE_TEMPLATE, 100));
		this.assertLimiter("adminid", expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_limiter_accountNotFound() throws Exception {
		var expectedResponse = new ResponseRelayDTO(false, "404 NOT_FOUND \"Account not found\"");
		this.assertLimiter("NOT_FOUND", expectedResponse, 404);
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() throws Exception {
		//given
		givenAccount("masteradminid");

		var plan = Plan.loadDefault();
		plan.getFeature("switcher").setValue(-1);
		plan.setName("UNLIMITED");

		planService.createPlan(plan);
		accountService.updateAccountPlan("masteradminid", "UNLIMITED");
		
		//test
		var expectedResponse = new ResponseRelayDTO(true);
		this.assertValidate("masteradminid", "switcher",
				10000, expectedResponse, 200);
	}

	@Test
	void shouldReturnTrue() throws Exception {
		//given
		givenAccount("adminid_ok", "TEST");

		//test
		var expectedResponse = new ResponseRelayDTO(true);
		this.assertValidate("adminid_ok", "feature_integer",
				0, expectedResponse, 200);
	}

	@Test
	void shouldReturnFalse() throws Exception {
		//given
		givenAccount("adminid_nok", "TEST");

		//test
		var expectedResponse = new ResponseRelayDTO(false, "Feature limit has been reached");
		this.assertValidate("adminid_nok", "feature_integer",
				1, expectedResponse, 200);
	}

	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() throws Exception {
		var expectedResponse = new ResponseRelayDTO(false, "404 NOT_FOUND \"Account not found\"");
		this.assertValidate("NOT_FOUND", "domain",
				0, expectedResponse, 404);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() throws Exception {
		//given
		givenAccount("adminid");

		//test
		var expectedResponse = new ResponseRelayDTO(false, "400 BAD_REQUEST \"Invalid feature\"");
		this.assertValidate("adminid", "INVALID_FEATURE",
				0, expectedResponse, 400);
	}

}
