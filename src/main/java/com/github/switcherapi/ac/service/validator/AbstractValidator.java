package com.github.switcherapi.ac.service.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.switcherapi.ac.repository.AccountDao;

@Component
public abstract class AbstractValidator {
	
	public static final String ACCOUNT_NOT_FOUND = "Account not found";
	
	@Autowired
	protected AccountDao accountDao;
	
	protected boolean validate(int planValue, int accountValue) {
		if (planValue == -1)
			return false;
		return planValue <= accountValue;
	}

}
