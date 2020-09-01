package com.github.switcherac.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.github.switcherac.model.Account;
import com.github.switcherac.model.Plan;

@Component
public class AccountDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountRepository accountRepository;
	
	public Account findByAdminId(String adminId) {
		final Query query = new Query();
		query.addCriteria(Criteria.where("adminId").is(adminId));
		return mongoTemplate.findOne(query, Account.class);
	}
	
	public List<Account> findByPlanName(String planName) {
		final Plan planFound = planDao.findByName(planName);
		
		if (planFound != null) {
			final Query query = new Query();
			query.addCriteria(Criteria.where("plan").is(planFound));		
			
			return mongoTemplate.find(query, Account.class);
		}
		
		return Collections.emptyList();
	}
	
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

}
