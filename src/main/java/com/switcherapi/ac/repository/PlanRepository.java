package com.switcherapi.ac.repository;

import com.switcherapi.ac.model.domain.Plan;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanRepository extends MongoRepository<Plan, String> {
	
}
