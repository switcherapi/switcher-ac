package com.github.switcherapi.ac.service;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.repository.AccountDao;

@Service
public class AccountControlService {
	
	private final String ACCOUNT_NOT_FOUND = "Account not found";
	
	@Autowired
	private AccountDao accountDao;
	
	public ResponseRelay checkDomain(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxDomains(), total)) {
				return new ResponseRelay(false, "Domain limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkGroup(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxGroups(), total)) {
				return new ResponseRelay(false, "Group limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkSwitcher(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxSwitchers(), total)) {
				return new ResponseRelay(false, "Switcher limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkEnvironment(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxEnvironments(), total)) {
				return new ResponseRelay(false, "Environment limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkComponent(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxComponents(), total)) {
				return new ResponseRelay(false, "Component limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkTeam(String adminId, int total) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (validate(account.getPlan().getMaxTeams(), total)) {
				return new ResponseRelay(false, "Team limit has been reached");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
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
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkMetrics(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (!account.getPlan().isEnableMetrics()) {
				return new ResponseRelay(false, "Metrics is not available");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	public ResponseRelay checkHistory(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account != null) {
			if (!account.getPlan().isEnableHistory()) {
				return new ResponseRelay(false, "History is not available");
			}
		} else {
			return new ResponseRelay(false, ACCOUNT_NOT_FOUND);
		}
		
		return new ResponseRelay(true);
	}
	
	private boolean validate(int planValue, int accountValue) {
		if (planValue == -1)
			return false;
		return planValue <= accountValue;
	}

}
