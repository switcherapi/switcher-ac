package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveMongoRepository<Account, String> {

	Mono<Void> deleteByAdminId(String adminId);

}
