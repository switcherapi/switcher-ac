package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static com.github.switcherapi.ac.model.domain.Feature.DOMAIN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ValidatorFactoryTest {
	
	@Autowired ValidatorFactory validatorFactory;
	@Autowired AccountService accountService;
	
	@Test
	void shouldThrowError_requestIsEmpty() {
		var request = FeaturePayload.builder().build();
		assertThrows(ResponseStatusException.class, () -> validatorFactory.runValidator(request));
	}
	
	@Test
	void shouldThrowError_missingParameter() {
		var request = FeaturePayload.builder()
				.feature(DOMAIN.getValue())
				.owner("adminid")
				.build();
		
		assertThrows(ResponseStatusException.class, () -> validatorFactory.runValidator(request));
	}
	
	@Test
	void shouldThrowError_missingAdminId() {
		var request = FeaturePayload.builder()
				.feature(DOMAIN.getValue())
				.total(0)
				.build();
		
		assertThrows(ResponseStatusException.class, () -> validatorFactory.runValidator(request));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"rate_limit"
	})
	void shouldNotThrowError(String feature) {
		accountService.createAccount("adminid");
		
		var request = FeaturePayload.builder()
				.feature(feature)
				.owner("adminid")
				.total(0)
				.build();
		
		assertDoesNotThrow(() -> validatorFactory.runValidator(request));
	}

}
