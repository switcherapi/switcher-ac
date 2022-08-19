package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class SwitcherRelayControllerTests {
	
	@Autowired AccountService accountService;
	@Autowired PlanService planService;
	
	@Autowired
	MockMvc mockMvc;
	
	private final Gson GSON = new Gson();
	
	@Test
	void shoutNotCreate_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/switcher/v1/create")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf())
			.content(""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void testServices() {
		assertThat(accountService).isNotNull();
	}
	
	@Test
	void shouldCreateAccount() throws Exception {
		//given
		var jsonRequest = givenRequest("adminid");
		var jsonResponse = givenExpectedResponse(true);
		
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
		var jsonResponse = givenExpectedResponse(true);
		
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
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);
		
		//test
		this.executeTestExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution() throws Exception {
		//given
		givenAccount("adminid");
		Plan plan = Plan.loadDefault();
		plan.setMaxDailyExecution(0);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);

		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false,
				"Daily execution limit has been reached");

		//test
		this.executeTestExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution_accountNotFound() throws Exception {
		//given
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false,
				"404 NOT_FOUND \"Account not found\"");
		
		//test
		this.executeTestExecution("NOT_FOUND", expectedResponse, 404);
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() throws Exception {
		//given
		givenAccount("masteradminid");
		Plan plan = Plan.loadDefault();
		plan.setMaxSwitchers(-1);
		plan.setName("UNLIMITED");
		planService.createPlan(plan);
		accountService.updateAccountPlan("masteradminid", "UNLIMITED");
		
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);
		
		//test
		this.executeTestValidate("masteradminid", "switcher",
				10000, expectedResponse, 200);
	}
	
	static Stream<Arguments> providedOkValidators() {
	    return Stream.of(
	      Arguments.of("component", 1),
	      Arguments.of("domain", 0),
	      Arguments.of("environment", 1),
	      Arguments.of("group", 1),
	      Arguments.of("switcher", 1),
	      Arguments.of("team", 0)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("providedOkValidators")
	void shouldBeOkWhenValidate(String validator, int total) throws Exception {
		//given
		givenAccount("adminid");
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);

		//test
		this.executeTestValidate(
				"adminid", validator, total, expectedResponse, 200);
	}
	
	static Stream<Arguments> providedNokValidators() {
	    return Stream.of(
	      Arguments.of("component", 3, "Component limit has been reached"),
	      Arguments.of("domain", 2, "Domain limit has been reached"),
	      Arguments.of("environment", 3, "Environment limit has been reached"),
	      Arguments.of("group", 5, "Group limit has been reached"),
	      Arguments.of("switcher", 4, "Switcher limit has been reached"),
	      Arguments.of("team", 2, "Team limit has been reached")
	    );
	}
	
	@ParameterizedTest
	@MethodSource("providedNokValidators")
	void shouldNotBeOkWhenValidate(String validator, int total, String message) throws Exception {
		//given
		givenAccount("adminid");
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false, message);

		//test
		this.executeTestValidate(
				"adminid", validator, total, expectedResponse, 200);
	}
	
	
	@Test
	void shouldBeOkWhenValidate_metric() throws Exception {
		//given
		givenAccount("adminid");
		Plan plan = Plan.loadDefault();
		plan.setEnableMetrics(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);

		//test
		this.executeTestValidate("adminid", "metrics",
				null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_metric() throws Exception {
		//given
		givenAccount("adminid");
		Plan plan = Plan.loadDefault();
		plan.setEnableMetrics(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false,
				"Metrics is not available");
		
		//test
		this.executeTestValidate("adminid", "metrics", null, expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_history() throws Exception {
		//given
		givenAccount("adminid");
		Plan plan = Plan.loadDefault();
		plan.setEnableHistory(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);

		//test
		this.executeTestValidate("adminid", "history",
				null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_history() throws Exception {
		//given
		givenAccount("adminid");
		Plan plan = Plan.loadDefault();
		plan.setEnableHistory(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false, "History is not available");
		
		//test
		this.executeTestValidate("adminid", "history", null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() throws Exception {
		//given
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false,
				"404 NOT_FOUND \"Account not found\"");

		final String[] features = {
				"domain", "group", "switcher", "component",
				"environment", "team", "metrics", "history"
		};
		
		final Integer[] paramValue = {
				0, 0, 0, 0, 0, 0, null, null
		};
		
		//test
		for (int i = 0; i < features.length; i++) {
			this.executeTestValidate("NOT_FOUND", features[i], paramValue[i], expectedResponse, 404);
		}
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() throws Exception {
		//given
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false,
				"400 BAD_REQUEST \"Invalid validator: INVALID_FEATURE\"");
		
		//test
		this.executeTestValidate("adminid", "INVALID_FEATURE",
				0, expectedResponse, 400);
	}

	private void executeTestValidate(String adminId, String featureName,
		Integer value, ResponseRelayDTO expectedResponse, int expectedStatus) throws Exception {

		//given
		FeaturePayload feature = FeaturePayload.builder()
				.feature(featureName)
				.owner(adminId)
				.build();

		if (value != null)
			feature.setTotal(value);

		RequestRelayDTO request = new RequestRelayDTO();
		request.setPayload(GSON.toJson(feature));

		String jsonRequest = GSON.toJson(request);
		String jsonResponse = GSON.toJson(expectedResponse);

		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
						.with(csrf())
						.content(jsonRequest))
				.andDo(print())
				.andExpect(status().is(expectedStatus))
				.andExpect(content().string(containsString(jsonResponse)));
	}

	private void executeTestExecution(String value, ResponseRelayDTO expectedResponse,
									  int expectedStatus) throws Exception {
		//given
		String jsonResponse = GSON.toJson(expectedResponse);

		//test
		this.mockMvc.perform(get("/switcher/v1/execution")
						.contentType(MediaType.APPLICATION_JSON)
						.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
						.with(csrf())
						.queryParam("value", value))
				.andDo(print())
				.andExpect(status().is(expectedStatus))
				.andExpect(content().string(containsString(jsonResponse)));
	}

	private String givenExpectedResponse(boolean result) {
		return givenExpectedResponse(result, null);
	}

	private String givenExpectedResponse(boolean result, String message) {
		ResponseRelayDTO response = new ResponseRelayDTO(result, message);
		return GSON.toJson(response);
	}

	private String givenRequest(String adminId) {
		RequestRelayDTO request = new RequestRelayDTO();
		request.setValue(adminId);

		return GSON.toJson(request);
	}

	private void givenAccount(String adminId) {
		accountService.createAccount(adminId);
	}

}
