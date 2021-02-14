package com.github.switcherapi.ac.service.validator.beans;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.AbstractValidator;
import com.github.switcherapi.ac.service.validator.SwitcherCheck;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("switcher")
public class ValidateSwitcher extends AbstractValidator {
	
	@SwitcherCheck
	public ResponseRelay checkSwitcher(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxSwitchers(), total)) {
				return new ResponseRelay(false, "Switcher limit has been reached");
			}
		} else {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}

}
