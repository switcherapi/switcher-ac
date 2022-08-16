package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ValidatorFactoryTest {
	
	@Autowired ValidatorFactory validatorFactory;
	@Autowired AccountService accountService;
	
	@Test
	void shouldThrowError_requestIsEmpty() {
		var request = FeaturePayload.builder().build();
		assertThrows(ResponseStatusException.class, () ->
				validatorFactory.runValidator(request));
	}
	
	@Test
	void shouldThrowError_missingParameter() {
		var request = FeaturePayload.builder()
				.feature("domain")
				.owner("adminid")
				.build();
		
		assertThrows(ResponseStatusException.class, () ->
				validatorFactory.runValidator(request));
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
		var request = FeaturePayload.builder()
				.feature(feature)
				.total(0)
				.build();
		
		assertThrows(ResponseStatusException.class, () ->
				validatorFactory.runValidator(request));
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
		
		var request = FeaturePayload.builder()
				.feature(feature)
				.owner("adminid")
				.total(0)
				.build();
		
		assertDoesNotThrow(() -> {
			validatorFactory.runValidator(request);
		});
	}

}
