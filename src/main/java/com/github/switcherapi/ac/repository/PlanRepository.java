package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Plan;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PlanRepository extends ReactiveMongoRepository<Plan, String> {
	
}
