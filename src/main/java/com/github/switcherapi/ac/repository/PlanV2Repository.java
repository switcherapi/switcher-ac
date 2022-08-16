package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.PlanV2;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlanV2Repository extends MongoRepository<PlanV2, String> {
	
}
