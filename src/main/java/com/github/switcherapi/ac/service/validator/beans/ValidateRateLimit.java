package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.Metadata;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

import static com.github.switcherapi.ac.config.SwitcherFeatures.SWITCHER_AC_METADATA;
import static com.github.switcherapi.ac.config.SwitcherFeatures.getSwitcher;
import static com.github.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

@SwitcherValidator("rate_limit")
public class ValidateRateLimit extends AbstractActiveCheckValidator {

	public static final String MESSAGE_TEMPLATE = "{ \"rate_limit\": %s }";

	public ValidateRateLimit(AccountDao accountDao) {
		super(accountDao);
	}

	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		final var plan = account.getPlan();
		final var max = Integer.parseInt(plan.getFeature(RATE_LIMIT).getValue().toString());

		if (getSwitcher(SWITCHER_AC_METADATA).isItOn()) {
			return ResponseRelayDTO.create(true).withMetadata(Metadata.builder().rateLimit(max).build());
		}

		return ResponseRelayDTO.create(true).withMessage(String.format(MESSAGE_TEMPLATE, max));
	}

}
