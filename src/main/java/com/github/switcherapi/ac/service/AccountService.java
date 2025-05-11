package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.mapper.AccountMapper;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

	public Mono<AccountDTO> createAccount(String adminId) {
		return createAccount(adminId, PlanType.DEFAULT.name());
	}

	public Mono<AccountDTO> createAccount(String adminId, String planName) {
		return planDao.findByName(planName)
				.switchIfEmpty(Mono.error(new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName))))
				.flatMap(plan -> accountDao.findByAdminId(adminId)
						.defaultIfEmpty(new Account(adminId))
						.flatMap(account -> {
							account.setPlan(plan.getId());
							return accountDao.getAccountRepository().save(account)
									.map(savedAccount -> AccountMapper.map(savedAccount, plan));
						}));
	}

	public Mono<Account> updateAccountPlan(String adminId, String planName) {
		return planDao.findByName(planName)
				.switchIfEmpty(Mono.error(new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName))))
				.flatMap(plan -> getAccountByAdminId(adminId)
						.flatMap(account -> {
							account.setPlan(plan.getId());
							return accountDao.getAccountRepository().save(account);
						}));
	}

	public Mono<Void> deleteAccount(String adminId) {
		return getAccountByAdminId(adminId)
				.flatMap(account -> accountDao.getAccountRepository().delete(account));
	}

	public Mono<Account> getAccountByAdminId(String adminId) {
		return accountDao.findByAdminId(adminId)
				.switchIfEmpty(Mono.error(new ResponseStatusException(
						HttpStatus.NOT_FOUND, String.format(ACCOUNT_NAME_NOT_FOUND.getValue(), adminId))));
	}

	public Flux<Account> getAccountsByPlanName(String planName) {
		return accountDao.findByPlanName(planName);
	}

}
