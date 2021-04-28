package com.github.switcherapi.ac.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.github.switcherapi.ac.model.Account;

@Component
public class AccountDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountRepository accountRepository;
	
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
	
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

}
