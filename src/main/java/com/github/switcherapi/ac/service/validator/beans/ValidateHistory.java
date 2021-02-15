package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("history")
public class ValidateHistory extends AbstractActiveCheckValidator {
	
	@Override
	protected ResponseRelay executeValidator(Account account) {
		if (!account.getPlan().getEnableHistory().booleanValue()) {
			return new ResponseRelay(false, "History is not available");
		}
		
		return new ResponseRelay(true);
	}

}
