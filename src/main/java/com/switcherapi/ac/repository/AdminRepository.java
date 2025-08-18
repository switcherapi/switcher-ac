package com.switcherapi.ac.repository;

import com.switcherapi.ac.model.domain.Admin;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AdminRepository extends ReactiveMongoRepository<Admin, String> {
	
	Mono<Admin> findByGitHubId(String gitHubId);
	
	Mono<Admin> findByToken(String token);

}
