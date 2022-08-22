package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Plan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanRepository extends MongoRepository<Plan, String> {
	
}
