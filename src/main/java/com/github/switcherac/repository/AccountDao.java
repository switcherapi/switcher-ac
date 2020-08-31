package com.github.switcherac.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.github.switcherac.model.Account;

@Component
public class AccountDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private AccountRepository accountRepository;
	
	public Account findByAdminId(String adminId) {
		final Query query = new Query();
		query.addCriteria(Criteria.where("adminId").is(adminId));
		return mongoTemplate.findOne(query, Account.class);
	}
	
	public AccountRepository getAccountRepository() {
		return accountRepository;
	}

}
