package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.service.validator.ValidatorBuilderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import static com.github.switcherapi.ac.model.domain.Feature.RATE_LIMIT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {"service.validators.native=true"})
class ValidatorNativeBuilderServiceTest {
	
	@Autowired
	ValidatorBuilderService validatorBuilderService;
	@Autowired AccountService accountService;
	
	@Test
	void shouldThrowError_requestIsEmpty() {
		var request = FeaturePayload.builder().build();
		assertThrows(ResponseStatusException.class, () -> validatorBuilderService.runValidator(request));
	}
	
	@Test
	void shouldThrowError_missingAdminId() {
		var request = FeaturePayload.builder()
				.feature(RATE_LIMIT.getValue())
				.build();
		
		assertThrows(ResponseStatusException.class, () -> validatorBuilderService.runValidator(request));
	}

	@Test
	void shouldNotThrowError() {
		accountService.createAccount("adminid").block();
		
		var request = FeaturePayload.builder()
				.feature(RATE_LIMIT.getValue())
				.owner("adminid")
				.build();
		
		assertDoesNotThrow(() -> validatorBuilderService.runValidator(request));
	}

}
