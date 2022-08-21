package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.Map;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;

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

	public Object getParam(SwitcherValidatorParams param) {
		return params.get(param);
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

	/**
	 * Default request validation.
	 * It adds ADMINID and TOTAL params to the validator input
	 */
	protected abstract void validateRequest(FeaturePayload request);

}
