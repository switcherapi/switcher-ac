package com.github.switcherapi.ac.service.validator.beans;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.validator.SwitcherValidator;

@SwitcherValidator("daily_execution")
public class ValidateExecution extends AbstractActiveCheckValidator {
	
	@Override
	protected ResponseRelayDTO executeValidator(final Account account) {
		final var dateTime = new DateTime(new Date());
		final var lastReset = new DateTime(account.getLastReset());
		final int days = Days.daysBetween(lastReset, dateTime).getDays();
		
		if (days >= 1) {
			account.setCurrentDailyExecution(0);
			account.setLastReset(dateTime.toDate());
		}
		
		if (validate(account.getCurrentDailyExecution(),
				Integer.parseInt(account.getPlanV2().getFeature("daily_execution").getValue().toString()))) {
			account.setCurrentDailyExecution(account.getCurrentDailyExecution() + 1);
			accountDao.getAccountRepository().save(account);				
		} else {
			return new ResponseRelayDTO(false, "Daily execution limit has been reached");
		}
		
		return new ResponseRelayDTO(true);
	}

}
