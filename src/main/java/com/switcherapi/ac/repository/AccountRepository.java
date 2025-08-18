package com.switcherapi.ac.repository;

import com.switcherapi.ac.model.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

	Mono<Void> deleteByAdminId(String adminId);

}
