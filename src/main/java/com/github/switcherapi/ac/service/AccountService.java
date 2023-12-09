package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.util.Constants.ACCOUNT_NAME_NOT_FOUND;
import static com.github.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;

@Service
public class AccountService {
	
	private final PlanDao planDao;
	private final AccountDao accountDao;
	
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
					HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName));
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

	public void updateAccountPlan(String adminId, String planName) {
		final var plan = planDao.findByName(planName);
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName));
		}

		final var account = getAccountByAdminId(adminId);
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
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
					HttpStatus.NOT_FOUND, String.format(ACCOUNT_NAME_NOT_FOUND.getValue(), adminId));
		}
		
		return account;
	}

	public List<Account> getAccountsByPlanName(String planName) {
		return accountDao.findByPlanName(planName);
	}
	
}
