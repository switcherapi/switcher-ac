package com.github.switcherapi.ac.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
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

import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class MockAdminPlanControllerTests {
	
	@InjectMocks
    private AdminController adminController;
	
	@Mock
	private PlanService mockPlanService;
	
	private MockMvc mockMvc;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }
	
	@Test
	void shouldNotCreateNewPlan() throws Exception {
		//mock
        Mockito.when(mockPlanService.createPlan(Mockito.any(PlanDTO.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
		//given
        PlanDTO planObj = Plan.loadDefault();
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(post("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer api_token")
			.content(json))
			.andDo(print())
			.andExpect(status().is5xxServerError());
	}
	
	@Test
	void shouldNotListPlans() throws Exception {
		//mock
        Mockito.when(mockPlanService.listAll())
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		//test
		this.mockMvc.perform(get("/admin/plan/v1/list")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer api_token"))
			.andDo(print())
			.andExpect(status().is5xxServerError());
	}

}
