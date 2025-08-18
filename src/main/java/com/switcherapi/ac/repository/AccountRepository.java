package com.switcherapi.ac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.switcherapi.ac.model.domain.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	
}
