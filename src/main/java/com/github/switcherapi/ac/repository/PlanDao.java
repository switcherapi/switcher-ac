package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanV2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class PlanDao {

	private final MongoTemplate mongoTemplate;

	private final PlanRepository planRepository;

	private final PlanV2Repository planV2Repository;

	public PlanDao(
			MongoTemplate mongoTemplate,
			PlanRepository planRepository,
			PlanV2Repository planV2Repository) {
		this.mongoTemplate = mongoTemplate;
		this.planRepository = planRepository;
		this.planV2Repository = planV2Repository;
	}

	public Plan findByName(String name) {
		final var query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, Plan.class);
	}

	public PlanV2 findV2ByName(String name) {
		final var query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, PlanV2.class);
	}
	
	public void deleteByName(String planName) {
		final var query = new Query();
		query.addCriteria(Criteria.where("name").is(planName));
		mongoTemplate.remove(query, Plan.class);
	}
	
	public PlanRepository getPlanRepository() {
		return planRepository;
	}

	public PlanV2Repository getPlanV2Repository() {
		return planV2Repository;
	}

}
