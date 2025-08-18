package com.switcherapi.ac.repository;

import com.switcherapi.ac.model.domain.Plan;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PlanRepository extends ReactiveMongoRepository<Plan, String> {
	
}
