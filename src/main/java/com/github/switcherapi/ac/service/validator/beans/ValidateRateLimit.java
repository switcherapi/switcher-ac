package com.github.switcherapi.ac.service.validator.beans;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.Feature;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.Metadata;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;
import reactor.core.publisher.Mono;

@SwitcherValidator(ValidateRateLimit.RATE_LIMIT_VALIDATOR)
public class ValidateRateLimit extends AbstractActiveCheckValidator {

	public static final String RATE_LIMIT_VALIDATOR = "rate_limit";

	public ValidateRateLimit(AccountDao accountDao, PlanDao planDao) {
		super(accountDao, planDao);
	}

	@Override
	protected Mono<ResponseRelayDTO> executeValidator(final Account account) {
		return planDao.getPlanRepository().findById(account.getPlan())
				.switchIfEmpty(Mono.just(Plan.loadDefault()))
				.flatMap(plan -> {
					final var max = Integer.parseInt(plan.getFeature(Feature.RATE_LIMIT).getValue().toString());
					return Mono.just(ResponseRelayDTO.success(Metadata.builder().rateLimit(max).build()));
				});
	}

}
