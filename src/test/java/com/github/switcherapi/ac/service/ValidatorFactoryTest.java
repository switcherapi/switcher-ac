package com.github.switcherapi.ac.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

@SpringBootTest
class ValidatorFactoryTest {
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Autowired
	private AccountService accountService;
	
	@Test
	void shouldThrowError_requestIsEmpty() {
		FeaturePayload request = new FeaturePayload();
		assertThrows(ResponseStatusException.class, () -> {
			validatorFactory.runValidator(request);
		});
	}
	
	@Test
	void shouldThrowError_missingParameter() {
		FeaturePayload request = new FeaturePayload();
		request.setFeature("domain");
		request.setOwner("adminid");
		
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
	void shouldThrowError_missingAdminId(String feature) {
		FeaturePayload request = new FeaturePayload();
		request.setFeature(feature);
		request.setTotal(0);
		
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
	void shouldNotThrowError(String feature) {
		accountService.createAccount("adminid");
		
		FeaturePayload request = new FeaturePayload();
		request.setFeature(feature);
		request.setOwner("adminid");
		request.setTotal(0);
		
		assertDoesNotThrow(() -> {
			validatorFactory.runValidator(request);
		});
	}

}
