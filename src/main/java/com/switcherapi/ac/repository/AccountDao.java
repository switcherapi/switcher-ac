package com.github.switcherapi.ac.repository;

import com.github.switcherapi.ac.model.domain.Account;
import lombok.Getter;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class AccountDao {

    private final ReactiveMongoTemplate mongoTemplate;

    private final PlanDao planDao;

    @Getter
    private final AccountRepository accountRepository;

    public AccountDao(ReactiveMongoTemplate mongoTemplate, PlanDao planDao, AccountRepository accountRepository) {
        this.mongoTemplate = mongoTemplate;
        this.planDao = planDao;
        this.accountRepository = accountRepository;
    }

    public Mono<Account> findByAdminId(String adminId) {
        var query = new Query();
        query.addCriteria(Criteria.where("adminId").is(adminId));
        return mongoTemplate.findOne(query, Account.class);
    }

    public Flux<Account> findByPlanName(String planName) {
        return planDao.findByName(planName)
                .flatMapMany(plan -> {
                    var query = new Query();
                    query.addCriteria(Criteria.where("plan").is(plan.getId()));
                    return mongoTemplate.find(query, Account.class);
                });
    }

}
