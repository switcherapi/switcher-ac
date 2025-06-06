package com.github.switcherapi.ac.service.validator;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.ADMINID;
import static com.github.switcherapi.ac.util.Constants.ACCOUNT_NOT_FOUND;

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
	public Mono<ResponseRelayDTO> execute(FeaturePayload request) {
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
	protected Mono<ResponseRelayDTO> executeValidator() {
		return accountDao.findByAdminId(getParam(ADMINID, String.class))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND.getValue())))
				.flatMap(this::executeValidator);
	}

	/**
	 * Executes validator bean
	 */
	protected abstract Mono<ResponseRelayDTO> executeValidator(final Account account);

	/**
	 * Default request validation.
	 * It adds ADMINID and TOTAL params to the validator input
	 */
	protected abstract void validateRequest(FeaturePayload request);

}
