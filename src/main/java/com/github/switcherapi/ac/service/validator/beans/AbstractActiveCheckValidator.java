package com.github.switcherapi.ac.service.validator.beans;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

public abstract class AbstractActiveCheckValidator extends AbstractValidatorService {
	
	@Override
	public void validateRequest(RequestRelay request) {
		final String[] args = request.getValue().split(ValidatorFactory.SEPARATOR);
		
		try {
			params.put(ADMINID, args[1]);
		} catch (Exception e) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, String.format("Invalid input for: %s", args[0]));
		}
	}
	
	protected abstract ResponseRelay executeValidator(final Account account);

}
