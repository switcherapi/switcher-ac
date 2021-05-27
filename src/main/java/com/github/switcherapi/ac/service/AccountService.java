package com.github.switcherapi.ac.service;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;

@Service
public class AccountService {
	
	private static final String PLAN_NOT_FOUND = "Unable to find plan named %s";
	
	private static final String ACCOUNT_NOT_FOUND = "Unable to find account %s";
	
	private PlanDao planDao;
	
	private AccountDao accountDao;
	
	public AccountService(PlanDao planDao, AccountDao accountDao) {
		this.planDao = planDao;
		this.accountDao = accountDao;
	}

	public Account createAccount(String adminId) {
		return createAccount(adminId, PlanType.DEFAULT.name());
	}
	
	public Account createAccount(String adminId, String planName) {
		final var plan = planDao.findByName(planName);
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		var account = accountDao.findByAdminId(adminId);
		if (account == null) {
			account = new Account();
			account.setAdminId(adminId);
		}
		
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
		
		return account;
	}
	
	public Account resetDailyExecution(String adminId) {
		var account = getAccountByAdminId(adminId);
		account.setCurrentDailyExecution(0);
		account.setLastReset(new Date());
		accountDao.getAccountRepository().save(account);
		return account;
	}
	
	public Account updateAccountPlan(String adminId, String planName) {
		final var plan = planDao.findByName(planName);
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		final var account = getAccountByAdminId(adminId);
		
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
		
		return account;
	}
	
	public void deleteAccount(String adminId) {
		final var account = getAccountByAdminId(adminId);
		if (account != null) {
			accountDao.getAccountRepository().delete(account);
		}	
	}
	
	public Account getAccountByAdminId(String adminId) {
		final var account = accountDao.findByAdminId(adminId);
		
		if (account == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(ACCOUNT_NOT_FOUND, adminId));
		}
		
		return account;
	}
	
	public List<Account> getAccountsByPlanName(String planName) {
		return accountDao.findByPlanName(planName);
	}
	
}
