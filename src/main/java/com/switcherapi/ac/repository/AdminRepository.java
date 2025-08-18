package com.switcherapi.ac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.switcherapi.ac.model.domain.Admin;

public interface AdminRepository extends MongoRepository<Admin, String> {
	
	Admin findByGitHubId(String gitHubId);
	
	Admin findByToken(String token);

}
