package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("metrics")
public class ValidateMetrics extends AbstractActiveCheckValidator {

	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		if (!account.getPlan().getEnableMetrics().booleanValue()) {
			return new ResponseRelayDTO(false, "Metrics is not available");
		}
		
		return new ResponseRelayDTO(true);
	}

}
