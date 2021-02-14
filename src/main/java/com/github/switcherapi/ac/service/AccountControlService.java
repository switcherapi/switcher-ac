package com.github.switcherapi.ac.service;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.repository.AccountDao;

@Service
public class AccountControlService {
	
	private static final String ACCOUNT_NOT_FOUND = "Account not found";
	
	@Autowired
	private AccountDao accountDao;
	
	public ResponseRelay checkExecution(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
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
		} else {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	private boolean validate(int planValue, int accountValue) {
		if (planValue == -1)
			return false;
		return planValue <= accountValue;
	}

}
