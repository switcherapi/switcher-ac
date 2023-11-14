package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("rate_limit")
public class ValidateRateLimit extends AbstractActiveCheckValidator {

	public static final String MESSAGE_TEMPLATE = "{ \"rate_limit\": %s }";

	public ValidateRateLimit(AccountDao accountDao) {
		super(accountDao);
	}

	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		final var plan = account.getPlan();
		final var max = Integer.parseInt(plan.getFeature("rate_limit").getValue().toString());
		return new ResponseRelayDTO(true, String.format(MESSAGE_TEMPLATE, max));
	}

}
