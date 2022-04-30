package com.github.switcherapi.ac.service.validator.beans;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.TOTAL;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("environment")
public class ValidateEnvironment extends AbstractValidatorService {
	
	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		if (validate(account.getPlan().getMaxEnvironments(), 
				getParam(TOTAL, Integer.class))) {
			return new ResponseRelayDTO(false, "Environment limit has been reached");
		}
		
		return new ResponseRelayDTO(true);
	}

}
