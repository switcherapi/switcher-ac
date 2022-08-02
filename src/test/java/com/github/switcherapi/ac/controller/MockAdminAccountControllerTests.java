package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.PlanService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class MockAdminAccountControllerTests {

	@Mock private PlanService mockPlanService;
	@Mock private AccountService mockAccountService;
	@Mock private AdminService mockAdminService;
	
	private MockMvc mockMvc;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
		final var adminController = new AdminController(mockPlanService, mockAccountService, mockAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }
	
	@Test
	void shouldNotResetDailyExecution() throws Exception {
		//given
        Mockito.when(mockAccountService.resetDailyExecution(Mockito.any(String.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
		//test
		this.mockMvc.perform(patch("/admin/v1/account/reset/{adminId}", "mock_account1")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer api_token"))
				.andDo(print())
				.andExpect(status().is5xxServerError());
	}

}
