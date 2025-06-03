package com.github.switcherapi.ac.service.validator.beans;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;

import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;

public abstract class AbstractActiveCheckValidator extends AbstractValidatorService {

	protected AbstractActiveCheckValidator(AccountDao accountDao, PlanDao planDao) {
		super(accountDao, planDao);
	}

	@Override
	public void validateRequest(FeaturePayload request) {
		try {
			Assert.notNull(request.owner(), "Admin ID is missing");
			params.put(ADMINID, request.owner());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
	
	protected abstract ResponseRelayDTO executeValidator(final Account account);

}
