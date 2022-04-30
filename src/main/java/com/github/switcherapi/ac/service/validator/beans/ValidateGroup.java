package com.github.switcherapi.ac.service.validator.beans;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.TOTAL;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("group")
public class ValidateGroup extends AbstractValidatorService {

	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		if (validate(account.getPlan().getMaxGroups(), 
				getParam(TOTAL, Integer.class))) {
			return new ResponseRelayDTO(false, "Group limit has been reached");
		}
		
		return new ResponseRelayDTO(true);
	}

}
