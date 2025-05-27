package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.dto.Metadata;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.PlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;

import static com.github.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
@TestPropertySource(properties = {
		"service.cache.enabled=true",
		"service.cache.duration=1"
})
class SwitcherRelayCacheLimiterControllerTests extends ControllerTestUtils {

	@Autowired PlanService planService;

	@BeforeEach
	void setupPlan() {
		createPlan();
	}

	@Test
	void shouldReturnUnchangedRateLimit() {
		//given
		givenAccount("adminid", "TEST");

		//test
		var expectedResponse = ResponseRelayDTO.success(Metadata.builder().rateLimit(100).build());
		this.assertLimiter("adminid", expectedResponse, 200);

		//update plan rate limit to 200
		updatePlanRateLimit();

		//test again
		this.assertLimiter("adminid", expectedResponse, 200);
	}

	private void updatePlanRateLimit() {
		StepVerifier.create(planService.updatePlan("TEST", Plan.builder()
				.attributes(List.of(
						PlanAttribute.builder()
								.feature(RATE_LIMIT.getValue())
								.value(200)
								.build()))
				.build()))
				.expectNextCount(1)
				.verifyComplete();
	}

	private void createPlan() {
		StepVerifier.create(planService.createPlan(Plan.builder()
				.name("TEST")
				.attributes(List.of(
						PlanAttribute.builder()
								.feature(RATE_LIMIT.getValue())
								.value(100)
								.build()))
				.build()))
				.expectNextCount(1)
				.verifyComplete();
	}

}
