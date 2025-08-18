package com.switcherapi.ac.repository;

import com.switcherapi.ac.model.domain.Plan;
import lombok.Getter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class PlanDao {

    private final MongoTemplate mongoTemplate;

    @Getter
    private final PlanRepository planRepository;

    public PlanDao(
            MongoTemplate mongoTemplate,
            PlanRepository planRepository) {
        this.mongoTemplate = mongoTemplate;
        this.planRepository = planRepository;
    }

    public Plan findByName(String name) {
        final var query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        return mongoTemplate.findOne(query, Plan.class);
    }

    public long deleteByName(String planName) {
        final var query = new Query();
        query.addCriteria(Criteria.where("name").is(planName));
        return mongoTemplate.remove(query, Plan.class).getDeletedCount();
    }

}
