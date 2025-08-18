package com.switcherapi.ac.controller;

import com.switcherapi.ac.config.SwitcherFeatures;
import com.switcherapi.ac.model.dto.RequestRelayDTO;
import com.switcherapi.ac.service.AccountService;
import com.switcherapi.ac.service.ValidatorBasicService;
import com.switcherapi.ac.service.validator.ValidatorBuilderService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureDataMongo
class SwitcherRelayControllerErrorTest {

	@Mock private AccountService mockAccountService;
	@Mock private ValidatorBuilderService mockValidatorBuilderService;
	@Mock private ValidatorBasicService mockValidatorBasicService;
	@Mock private SwitcherFeatures mockSwitcherConfig;
	
	private WebTestClient webTestClient;
	
	@BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
		final var switcherRelayController = new SwitcherRelayController(
				mockAccountService, mockValidatorBuilderService, mockValidatorBasicService, mockSwitcherConfig);
		webTestClient = WebTestClient.bindToController(switcherRelayController).build();
    }
	
	@Test
	void shouldNotCreateAccount() {
		//mock
        Mockito.when(mockAccountService.createAccount(Mockito.any(String.class)))
        	.thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        //given
  		var request = new RequestRelayDTO("adminid", null);
  		var jsonRequest = new Gson().toJson(request);
        
		//test
		webTestClient.post()
				.uri("/switcher/v1/create")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
				.bodyValue(jsonRequest)
				.exchange()
				.expectStatus().is5xxServerError();
	}

}
