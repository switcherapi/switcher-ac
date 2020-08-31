package com.github.switcherac.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.github.switcherac.model.Plan;

@Component
public class PlanDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private PlanRepository planRepository;
	
	public Plan findByName(String name) {
		final Query query = new Query();
		query.addCriteria(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, Plan.class);
	}
	
	public PlanRepository getPlanRepository() {
		return planRepository;
	}

}
