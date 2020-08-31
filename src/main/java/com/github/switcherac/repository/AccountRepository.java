package com.github.switcherac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.switcherac.model.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	
}
