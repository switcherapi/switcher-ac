package com.switcherapi.ac.controller;

import com.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.domain.PlanAttribute;
import com.switcherapi.ac.model.dto.Metadata;
import com.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.switcherapi.ac.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.switcherapi.ac.model.domain.Feature.DOMAIN;
import static com.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@Execution(ExecutionMode.CONCURRENT)
@TestPropertySource(properties = {
		"service.cache.enabled=true",
		"service.cache.duration=1"
})
class SwitcherRelayCacheLimiterControllerTest extends ControllerTestUtils {

	private static final String TEST_PLAN = "TEST";

	@Autowired PlanService planService;

	@BeforeEach
	void setupPlan() {
		createPlan();
	}

	@Test
	void shouldReturnUnchangedRateLimit() throws Exception {
		//given
		givenAccount("adminid", TEST_PLAN);

		//test
		var expectedResponse = ResponseRelayDTO.success(Metadata.builder().rateLimit(100).build());
		this.assertLimiter("adminid", expectedResponse, 200);

		//update plan rate limit to 200
		updatePlanFeature(RATE_LIMIT.getValue(), 200);

		//test again
		this.assertLimiter("adminid", expectedResponse, 200);
	}

	@Test
	void shouldReturnUnchangedValidationResponse() throws Exception {
		//given
		givenAccount("adminid", TEST_PLAN);

		//test
		var expectedResponse = ResponseRelayDTO.fail("Feature limit has been reached");
		this.assertValidate("adminid", DOMAIN.getValue(),
				1, expectedResponse, 200);

		//update Domain feature limit to 2
		updatePlanFeature(DOMAIN.getValue(), 2);

		//test again
		this.assertValidate("adminid", DOMAIN.getValue(),
				1, expectedResponse, 200);
	}

	private void createPlan() {
		planService.createPlan(Plan.builder()
				.name(TEST_PLAN)
				.attributes(List.of(
						PlanAttribute.builder().feature(DOMAIN.getValue()).value(1).build(),
						PlanAttribute.builder().feature(RATE_LIMIT.getValue()).value(100).build()
				)).build());
	}

	private void updatePlanFeature(String feature, int value) {
		planService.updatePlan(TEST_PLAN, Plan.builder()
				.attributes(List.of(
						PlanAttribute.builder()
								.feature(feature)
								.value(value)
								.build()
				)).build());
	}

}
