package com.github.switcherapi.ac.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

@SpringBootTest
class ValidatorFactoryTest {
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Autowired
	private AccountService accountService;
	
	@Test
	void shouldThrowError_requestIsEmpty() {
		RequestRelay request = new RequestRelay();
		assertThrows(ResponseStatusException.class, () -> {
			validatorFactory.runValidator(request);
		});
	}
	
	@Test
	void shouldThrowError_missingParameter() {
		RequestRelay request = new RequestRelay();
		request.setValue("domain#adminid");
		
		assertThrows(ResponseStatusException.class, () -> {
			validatorFactory.runValidator(request);
		});
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"component",
			"domain",
			"environment",
			"group",
			"history",
			"metrics",
			"switcher",
			"team"
	}) 
	void shouldThrowError_missingAdminId(String validatorName) {
		RequestRelay request = new RequestRelay();
		request.setValue(validatorName);
		request.setNumeric("0");
		
		assertThrows(ResponseStatusException.class, () -> {
			validatorFactory.runValidator(request);
		});
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"component#adminid",
			"domain#adminid",
			"environment#adminid",
			"group#adminid",
			"history#adminid",
			"metrics#adminid",
			"switcher#adminid",
			"team#adminid"
	}) 
	void shouldNotThrowError(String validatorName) {
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue(validatorName);
		request.setNumeric("0");
		
		assertDoesNotThrow(() -> {
			validatorFactory.runValidator(request);
		});
	}

}
