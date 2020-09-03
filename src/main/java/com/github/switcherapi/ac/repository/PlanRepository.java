package com.github.switcherapi.ac.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.github.switcherapi.ac.model.Plan;

public interface PlanRepository extends MongoRepository<Plan, String> {
	
}
