package com.github.switcherapi.ac.controller;

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

import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.service.AccountService;
import com.google.gson.Gson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class MockSwitcherRelayControllerTests {
	
	@InjectMocks
    private SwitcherRelayController switcherRelayController;
	
	@Mock
	private AccountService mockAccountService;
	
	private MockMvc mockMvc;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(switcherRelayController).build();
    }
	
	@Test
	void shouldNotCreateAccount() throws Exception {
		//mock
        Mockito.when(mockAccountService.createAccount(Mockito.any(String.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        //given
  		RequestRelay request = new RequestRelay();
  		request.setValue("adminid");
  		String jsonRequest = new Gson().toJson(request);
        
		//test
		this.mockMvc.perform(post("/switcher/v1/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer relay_token")
				.content(jsonRequest))
				.andDo(print())
				.andExpect(status().is5xxServerError());
	}

}
