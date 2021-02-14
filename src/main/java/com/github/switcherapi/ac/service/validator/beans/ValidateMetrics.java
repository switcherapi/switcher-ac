package com.github.switcherapi.ac.service.validator.beans;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.AbstractValidator;
import com.github.switcherapi.ac.service.validator.SwitcherCheck;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("metrics")
public class ValidateMetrics extends AbstractValidator {
	
	@SwitcherCheck
	public ResponseRelay checkMetrics(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (!account.getPlan().getEnableMetrics().booleanValue()) {
				return new ResponseRelay(false, "Metrics is not available");
			}
		} else {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}

}
