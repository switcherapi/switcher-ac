package com.github.switcherac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.switcherac.model.Plan;

public interface PlanRepository extends MongoRepository<Plan, String> {
	
}
