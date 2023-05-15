package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.config.SwitcherConfig;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.ValidatorService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;
import com.google.gson.Gson;
import jakarta.ws.rs.core.HttpHeaders;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class MockSwitcherRelayControllerTests {

	@Mock private AccountService mockAccountService;
	@Mock private ValidatorFactory mockValidatorFactory;
	@Mock private ValidatorService mockValidatorService;
	@Mock private SwitcherConfig mockSwitcherConfig;
	
	private MockMvc mockMvc;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
		final var switcherRelayController = new SwitcherRelayController(
				mockAccountService, mockValidatorFactory, mockValidatorService, mockSwitcherConfig);
        mockMvc = MockMvcBuilders.standaloneSetup(switcherRelayController).build();
    }
	
	@Test
	void shouldNotCreateAccount() throws Exception {
		//mock
        Mockito.when(mockAccountService.createAccount(Mockito.any(String.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        //given
  		var request = new RequestRelayDTO();
  		request.setValue("adminid");
  		var jsonRequest = new Gson().toJson(request);
        
		//test
		this.mockMvc.perform(post("/switcher/v1/create")
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
					.content(jsonRequest))
				.andDo(print())
				.andExpect(status().is5xxServerError());
	}

}
