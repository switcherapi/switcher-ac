package com.github.switcherapi.ac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.switcherapi.ac.model.Admin;

public interface AdminRepository extends MongoRepository<Admin, String> {
	
	Admin findByGitHubId(String gitHubId);
	
	Admin findByToken(String token);

}
