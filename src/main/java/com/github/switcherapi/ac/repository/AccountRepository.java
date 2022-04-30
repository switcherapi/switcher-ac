package com.github.switcherapi.ac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.switcherapi.ac.model.domain.Account;

public interface AccountRepository extends MongoRepository<Account, String> {
	
}
