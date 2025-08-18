package com.switcherapi.ac.service;

import com.switcherapi.ac.model.domain.PlanType;
import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.mapper.PlanMapper;
import com.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;

@Service
public class PlanService {

    private final PlanDao planDao;

    private final AccountService accountService;

    public PlanService(PlanDao planDao, AccountService accountService) {
        this.planDao = planDao;
        this.accountService = accountService;
    }

    public Plan createPlan(Plan plan) {
        var planFound = Optional.ofNullable(planDao.findByName(plan.getName()));
        var newPlan = planFound.orElse(new Plan());

        PlanMapper.copyProperties(plan, newPlan);
        return planDao.getPlanRepository().save(newPlan);
    }

    public Plan updatePlan(String planName, Plan plan) {
        var planFound = getPlanByName(planName);
        PlanMapper.copyProperties(plan, planFound);
        planDao.getPlanRepository().save(planFound);

        return planFound;
    }

    public void deletePlan(String planName) {
        if (PlanType.DEFAULT.name().equals(planName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid plan name");
        }

        final var accountsWithPlan = accountService.getAccountsByPlanName(planName);
        accountsWithPlan.forEach(account ->
                accountService.updateAccountPlan(account.getAdminId(), PlanType.DEFAULT.name()));

        if (planDao.deleteByName(planName) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName));
        }
    }

    public List<Plan> listAll() {
        return planDao.getPlanRepository().findAll();
    }

    public Plan getPlanByName(String planName) {
        return Optional.ofNullable(planDao.findByName(planName))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName)));
    }

}
