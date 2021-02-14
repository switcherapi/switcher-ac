package com.github.switcherapi.ac.service.validator.beans;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.AbstractValidator;
import com.github.switcherapi.ac.service.validator.SwitcherCheck;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("history")
public class ValidateHistory extends AbstractValidator {
	
	@SwitcherCheck
	public ResponseRelay checkHistory(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (!account.getPlan().getEnableHistory().booleanValue()) {
				return new ResponseRelay(false, "History is not available");
			}
		} else {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}

}
