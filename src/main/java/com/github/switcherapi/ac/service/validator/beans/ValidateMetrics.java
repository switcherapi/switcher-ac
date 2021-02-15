package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("metrics")
public class ValidateMetrics extends AbstractActiveCheckValidator {

	@Override
	protected ResponseRelay executeValidator(Account account) {
		if (!account.getPlan().getEnableMetrics().booleanValue()) {
			return new ResponseRelay(false, "Metrics is not available");
		}
		
		return new ResponseRelay(true);
	}

}
