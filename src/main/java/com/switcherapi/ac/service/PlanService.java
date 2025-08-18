package com.switcherapi.ac.service;

import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.domain.PlanType;
import com.switcherapi.ac.model.mapper.PlanMapper;
import com.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;

@Service
public class PlanService {

    private final PlanDao planDao;

    private final AccountService accountService;

    public PlanService(PlanDao planDao, AccountService accountService) {
        this.planDao = planDao;
        this.accountService = accountService;
    }

    public Mono<Plan> createPlan(Plan plan) {
        return planDao.findByName(plan.getName())
                .flatMap(existingPlan -> {
                    PlanMapper.copyProperties(plan, existingPlan);
                    return planDao.getPlanRepository().save(existingPlan);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    var newPlan = new Plan();
                    PlanMapper.copyProperties(plan, newPlan);
                    return planDao.getPlanRepository().save(newPlan);
                }));
    }

    public Mono<Plan> updatePlan(String planName, Plan plan) {
        return getPlanByName(planName)
                .flatMap(existingPlan -> {
                    PlanMapper.copyProperties(plan, existingPlan);
                    return planDao.getPlanRepository().save(existingPlan);
                });
    }

    public Mono<Long> deletePlan(String planName) {
        if (PlanType.DEFAULT.name().equals(planName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan name");
        }

        return accountService.getAccountsByPlanName(planName)
                .flatMap(account -> accountService.updateAccountPlan(account.getAdminId(), PlanType.DEFAULT.name()))
                .then(planDao.deleteByName(planName)
                .flatMap(deleteResult -> {
                    if (deleteResult.getDeletedCount() == 0) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName)));
                    }

                    return Mono.just(deleteResult.getDeletedCount());
                }));

    }

    public Flux<Plan> listAll() {
        return planDao.getPlanRepository().findAll();
    }

    public Mono<Plan> getPlanByName(String planName) {
        return planDao.findByName(planName)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName))));
    }

}
