package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
		final var plan = Optional.ofNullable(planDao.findByName(planName))
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName)));

		var account = Optional.ofNullable(accountDao.findByAdminId(adminId))
				.orElse(new Account(adminId));

		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);

		return account;
	}

	public void updateAccountPlan(String adminId, String planName) {
		final var plan = Optional.ofNullable(planDao.findByName(planName))
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName)));

		final var account = getAccountByAdminId(adminId);
		account.setPlan(plan);
		accountDao.getAccountRepository().save(account);
	}

	public void deleteAccount(String adminId) {
		final var account = getAccountByAdminId(adminId);
		if (Objects.nonNull(account)) {
			accountDao.getAccountRepository().delete(account);
		}
	}

	public Account getAccountByAdminId(String adminId) {
		return Optional.ofNullable(accountDao.findByAdminId(adminId))
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(ACCOUNT_NAME_NOT_FOUND.getValue(), adminId)));
	}

	public List<Account> getAccountsByPlanName(String planName) {
		return accountDao.findByPlanName(planName);
	}

}
