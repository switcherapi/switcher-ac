package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.config.SwitcherFeatures;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;
import com.github.switcherapi.client.SwitcherMock;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.HttpHeaders;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class SwitcherRelayV2ControllerTests {

	@Autowired PlanService planService;
	@Autowired AccountService accountService;
	@Autowired MockMvc mockMvc;
	
	private final Gson GSON = new Gson();

	@BeforeEach
	void setupPlan() {
		planService.createPlanV2(PlanV2.builder()
			.name("TEST")
			.attributes(List.of(
					PlanAttribute.builder().feature("feature_integer").value(1).build()
			)).build());
	}
	
	@ParameterizedTest()
	@SwitcherMock(key = SwitcherFeatures.VALIDATOR_V2, result = true)
	void shouldReturnTrue() throws Exception {
		//given
		givenAccount("adminid_ok");

		//test
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(true);
		this.executeTestValidate("adminid_ok", "feature_integer",
				0, expectedResponse, 200);
	}

	@ParameterizedTest()
	@SwitcherMock(key = SwitcherFeatures.VALIDATOR_V2, result = true)
	void shouldReturnFalse() throws Exception {
		//given
		givenAccount("adminid_nok");

		//test
		ResponseRelayDTO expectedResponse = new ResponseRelayDTO(false, "Feature limit has been reached");
		this.executeTestValidate("adminid_nok", "feature_integer",
				1, expectedResponse, 200);
	}

	private void executeTestValidate(String adminId, String featureName,
									 Integer value, ResponseRelayDTO expectedResponse, int expectedStatus) throws Exception {
		//given
		var feature = FeaturePayload.builder()
				.feature(featureName)
				.owner(adminId)
				.total(value)
				.build();

		var request = new RequestRelayDTO();
		request.setPayload(GSON.toJson(feature));

		var jsonRequest = GSON.toJson(request);
		var jsonResponse = GSON.toJson(expectedResponse);

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

	private void givenAccount(String adminId) {
		accountService.createAccountV2(adminId, "TEST");
	}

}
