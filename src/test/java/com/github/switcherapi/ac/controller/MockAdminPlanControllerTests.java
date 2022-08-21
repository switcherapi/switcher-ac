package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.HttpHeaders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class MockAdminPlanControllerTests {

	@Mock private PlanService mockPlanService;

	private final Gson gson = new Gson();
	private MockMvc mockMvc;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
		final var planService = new PlanController(mockPlanService);
        mockMvc = MockMvcBuilders.standaloneSetup(planService).build();
    }
	
	@Test
	void shouldNotCreateNewPlan() throws Exception {
		//mock
        Mockito.when(mockPlanService.createPlanV2(Mockito.any(PlanV2.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
		//given
        var planObj = PlanV2.loadDefault();
		var json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(post("/plan/v2/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer api_token")
				.content(json))
			.andDo(print())
			.andExpect(status().is5xxServerError());
	}
	
	@Test
	void shouldNotListPlans() throws Exception {
		//mock
        Mockito.when(mockPlanService.listAllV2())
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		//test
		this.mockMvc.perform(get("/plan/v2/list")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer api_token"))
			.andDo(print())
			.andExpect(status().is5xxServerError());
	}

}
