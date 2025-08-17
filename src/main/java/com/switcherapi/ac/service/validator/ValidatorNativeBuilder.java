package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import com.github.switcherapi.ac.service.validator.beans.ValidateRateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static com.github.switcherapi.ac.service.validator.beans.ValidateRateLimit.RATE_LIMIT_VALIDATOR;

@Slf4j
@Component
@ConditionalOnProperty(value = "service.validators.native", havingValue = "true")
public class ValidatorNativeBuilder extends ValidatorBuilderService {

	public ValidatorNativeBuilder(AccountDao accountDao, PlanDao planDao) {
		super(accountDao, planDao);
		this.initializeValidators();
	}

	@Override
	protected void initializeValidators() {
		validatorHandlers.put(RATE_LIMIT_VALIDATOR, new ValidateRateLimit(accountDao, planDao));
	}

}
