package com.github.switcherapi.ac.service.validator.beans;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.TOTAL;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("team")
public class ValidateTeam extends AbstractValidatorService {
	
	@Override
	protected ResponseRelay executeValidator(final Account account) {
		if (validate(account.getPlan().getMaxTeams(), 
				getParam(TOTAL, Integer.class))) {
			return new ResponseRelay(false, "Team limit has been reached");
		}
		
		return new ResponseRelay(true);
	}

}
