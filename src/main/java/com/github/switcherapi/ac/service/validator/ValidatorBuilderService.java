package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

public abstract class ValidatorBuilderService {

	protected final Map<String, AbstractValidatorService> validatorHandlers = new HashMap<>();

	protected final AccountDao accountDao;

	protected ValidatorBuilderService(AccountDao accountDao) {
		this.accountDao = accountDao;
	}
    
    public ResponseRelayDTO runValidator(FeaturePayload request) {	
		if (!validatorHandlers.containsKey(request.feature())) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, String.format("Invalid validator: %s", request.feature()));
		}

		return validatorHandlers.get(request.feature()).execute(request);
    }

	protected abstract void initializeValidators();

}
