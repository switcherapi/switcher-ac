package com.switcherapi.ac.service.validator.beans;

import com.switcherapi.ac.model.domain.Account;
import com.switcherapi.ac.model.domain.Feature;
import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.dto.Metadata;
import com.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.switcherapi.ac.repository.AccountDao;
import com.switcherapi.ac.repository.PlanDao;
import com.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator(ValidateRateLimit.RATE_LIMIT_VALIDATOR)
public class ValidateRateLimit extends AbstractActiveCheckValidator {

	public static final String RATE_LIMIT_VALIDATOR = "rate_limit";

	public ValidateRateLimit(AccountDao accountDao, PlanDao planDao) {
		super(accountDao, planDao);
	}

	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		final var plan = planDao.getPlanRepository().findById(account.getPlan()).orElse(Plan.loadDefault());
		final var max = Integer.parseInt(plan.getFeature(Feature.RATE_LIMIT).getValue().toString());

		return ResponseRelayDTO.success(Metadata.builder().rateLimit(max).build());
	}

}
