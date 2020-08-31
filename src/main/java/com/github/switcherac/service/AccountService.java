package com.github.switcherac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherac.model.Account;
import com.github.switcherac.model.Plan;
import com.github.switcherac.repository.AccountDao;
import com.github.switcherac.repository.PlanDao;

public class AccountService {
	
	private final String PLAN_NOT_FOUND = "Unable to find plan named %s";
	
	private final String ACCOUNT_NOT_FOUND = "Unable to find account %s";
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountDao accountDao;
	
	public Account createAccount(String adminId, String planName) {
		final Plan plan = planDao.findByName(planName);
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		Account account = new Account();
		account.setAdminId(adminId);
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
		
		return account;
	}
	
	public Account updateAccountPlan(String adminId, String planName) {
		final Plan plan = planDao.findByName(planName);
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		final Account account = accountDao.findByAdminId(adminId);
		
		if (account == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(ACCOUNT_NOT_FOUND, adminId));
		}
		
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
		
		return account;
	}
	
	public void deleteAccount(String adminId) {
		final Account account = accountDao.findByAdminId(adminId);
		if (account != null) {
			accountDao.getAccountRepository().delete(account);
		} else {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(ACCOUNT_NOT_FOUND, adminId));
		}
			
	}
	
}
