package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
	
}
