package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Plan;
import com.mongodb.client.result.DeleteResult;
import lombok.Getter;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PlanDao {

    private final ReactiveMongoTemplate mongoTemplate;

    @Getter
    private final PlanRepository planRepository;

    public PlanDao(
            ReactiveMongoTemplate mongoTemplate,
            PlanRepository planRepository) {
        this.mongoTemplate = mongoTemplate;
        this.planRepository = planRepository;
    }

    public Mono<Plan> findByName(String name) {
        final var query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        return mongoTemplate.findOne(query, Plan.class);
    }

    public Mono<DeleteResult> deleteByName(String planName) {
        final var query = new Query();
        query.addCriteria(Criteria.where("name").is(planName));
        return mongoTemplate.remove(query, Plan.class);
    }

}
