package com.switcherapi.ac.service.validator;

import com.switcherapi.ac.model.domain.Account;
import com.switcherapi.ac.model.domain.FeaturePayload;
import com.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.switcherapi.ac.repository.AccountDao;
import com.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static com.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;
import static com.switcherapi.ac.util.Constants.ACCOUNT_NOT_FOUND;

public abstract class AbstractValidatorService {

	protected final AccountDao accountDao;

	protected final PlanDao planDao;

	protected Map<SwitcherValidatorParams, Object> params;

	protected AbstractValidatorService(AccountDao accountDao, PlanDao planDao) {
		this.accountDao = accountDao;
		this.planDao = planDao;
	}

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
		if (planValue == -1) {
			return false;
		}
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

		if (Objects.nonNull(account)) {
			return executeValidator(account);
		}

		throw new ResponseStatusException(HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND.getValue());
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
