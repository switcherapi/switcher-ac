package com.github.switcherapi.ac.service.validator.beans;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("execution")
public class ValidateExecution extends AbstractActiveCheckValidator {
	
	@Override
	protected ResponseRelay executeValidator(Account account) {
		final DateTime dateTime = new DateTime(new Date());
		final DateTime lastReset = new DateTime(account.getLastReset());
		final int days = Days.daysBetween(lastReset, dateTime).getDays();
		
		if (days >= 1) {
			account.setCurrentDailyExecution(0);
			account.setLastReset(dateTime.toDate());
		}
		
		if (validate(account.getCurrentDailyExecution(), account.getPlan().getMaxDailyExecution())) {
			account.setCurrentDailyExecution(account.getCurrentDailyExecution() + 1);
			accountDao.getAccountRepository().save(account);				
		} else {
			return new ResponseRelay(false, "Daily execution limit has been reached");
		}
		
		return new ResponseRelay(true);
	}

}
