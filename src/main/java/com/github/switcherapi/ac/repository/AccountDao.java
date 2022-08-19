package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Account;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AccountDao {

	private final MongoTemplate mongoTemplate;

	private final PlanDao planDao;

	private final AccountRepository accountRepository;

	public AccountDao(MongoTemplate mongoTemplate, PlanDao planDao, AccountRepository accountRepository) {
		this.mongoTemplate = mongoTemplate;
		this.planDao = planDao;
		this.accountRepository = accountRepository;
	}

	public Account findByAdminId(String adminId) {
		final var query = new Query();
		query.addCriteria(Criteria.where("adminId").is(adminId));
		return mongoTemplate.findOne(query, Account.class);
	}
	
	public List<Account> findByPlanName(String planName) {
		final var planFound = planDao.findByName(planName);
		
		if (planFound != null) {
			final var query = new Query();
			query.addCriteria(Criteria.where("plan").is(planFound));		
			
			return mongoTemplate.find(query, Account.class);
		}
		
		return Collections.emptyList();
	}

	public List<Account> findByPlanV2Name(String planName) {
		final var planFound = planDao.findV2ByName(planName);

		if (planFound != null) {
			final var query = new Query();
			query.addCriteria(Criteria.where("planV2").is(planFound));

			return mongoTemplate.find(query, Account.class);
		}

		return Collections.emptyList();
	}
	
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

}
