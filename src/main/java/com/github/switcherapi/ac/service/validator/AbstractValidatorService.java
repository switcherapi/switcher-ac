package com.github.switcherapi.ac.service.validator;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;
import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.TOTAL;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;

@Component
public abstract class AbstractValidatorService {
	
	public static final String ACCOUNT_NOT_FOUND = "Account not found";
	
	@Autowired
	protected AccountDao accountDao;
	
	protected Map<SwitcherValidatorParams, Object> params;
	
	/**
	 * Executes validator by validating the request and then calling the validator service
	 */
	public ResponseRelayDTO execute(FeaturePayload request) {
		params = new EnumMap<>(SwitcherValidatorParams.class);
		validateRequest(request);
		return executeValidator();
	}
	
	/**
	 * Returns the result of the executed validator
	 */
	protected boolean validate(int planValue, int accountValue) {
		if (planValue == -1)
			return false;
		return planValue <= accountValue;
	}
	
	public <T> T getParam(SwitcherValidatorParams param, Class<T> clazz) {
		return clazz.cast(params.get(param));
	}
	
	/**
	 * Default request validation.
	 * It adds ADMINID and TOTAL params to the validator input
	 */
	protected void validateRequest(FeaturePayload request) {
		try {
			Assert.notNull(request.getFeature(), "Feature is missing");
			Assert.notNull(request.getTotal(), "Total is missing");
			Assert.notNull(request.getOwner(), "Owner is missing");
			
			params.put(ADMINID, request.getOwner());
			params.put(TOTAL, request.getTotal());
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
	
	/**
	 * Default validator handler
	 */
	protected ResponseRelayDTO executeValidator() {
		final var account = accountDao.findByAdminId(
				getParam(ADMINID, String.class));
		
		if (account != null) {
			return executeValidator(account);
		}
		
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND);
	}
	
	/**
	 * Executes validator bean
	 */
	protected abstract ResponseRelayDTO executeValidator(final Account account);

}
