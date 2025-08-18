package com.switcherapi.ac.controller;

import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@Execution(ExecutionMode.CONCURRENT)
class AdminPlanControllerErrorTest {

	@Mock private PlanService mockPlanService;

	private final Gson gson = new Gson();
	private WebTestClient webTestClient;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
		final var planService = new PlanController(mockPlanService);
		webTestClient = WebTestClient.bindToController(planService).build();
    }
	
	@Test
	void shouldNotCreateNewPlan() {
		//mock
        Mockito.when(mockPlanService.createPlan(Mockito.any(Plan.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
		//given
        var planObj = Plan.loadDefault();
		var json = gson.toJson(planObj);
		
		//test
		webTestClient.post()
				.uri("/plan/v2/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer api_token")
				.bodyValue(json)
				.exchange()
			.expectStatus().is5xxServerError();
	}
	
	@Test
	void shouldNotListPlans() {
		//mock
        Mockito.when(mockPlanService.listAll())
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

		//test
		webTestClient.get()
				.uri("/plan/v2/list")
				.header(HttpHeaders.AUTHORIZATION, "Bearer api_token")
				.exchange()
			.expectStatus().is5xxServerError();
	}

}
