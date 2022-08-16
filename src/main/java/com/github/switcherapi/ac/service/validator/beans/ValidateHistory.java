package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("history")
public class ValidateHistory extends AbstractActiveCheckValidator {
	
	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		if (Boolean.FALSE.equals(account.getPlan().getEnableHistory())) {
			return new ResponseRelayDTO(false, "History is not available");
		}
		
		return new ResponseRelayDTO(true);
	}

}
