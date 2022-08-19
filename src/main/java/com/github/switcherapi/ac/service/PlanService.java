package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.model.mapper.DefaultMapper;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlanService {
	
	private static final String PLAN_NOT_FOUND = "Unable to find plan %s";

	private final PlanDao planDao;

	private final AccountService accountService;

	public PlanService(PlanDao planDao, AccountService accountService) {
		this.planDao = planDao;
		this.accountService = accountService;
	}

	public Plan createPlan(Plan plan) {
		var newPlan = planDao.findByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new Plan();
		
		DefaultMapper.copyProperties(plan, newPlan);
		return planDao.getPlanRepository().save(newPlan);
	}

	public PlanV2 createPlanV2(PlanV2 plan) {
		var newPlan = planDao.findV2ByName(plan.getName());
		newPlan = newPlan != null ? newPlan : PlanV2.builder().build();

		DefaultMapper.copyProperties(plan, newPlan);
		return planDao.getPlanV2Repository().save(newPlan);
	}
	
	public Plan updatePlan(String planName, Plan plan) {
		var planFound = getPlanByName(planName);
		DefaultMapper.copyProperties(plan, planFound);
		planDao.getPlanRepository().save(planFound);
		
		return planFound;
	}

	public PlanV2 updatePlanV2(String planName, PlanV2 plan) {
		var planFound = getPlanV2ByName(planName);
		DefaultMapper.copyProperties(plan, planFound);
		planDao.getPlanV2Repository().save(planFound);

		return planFound;
	}
	
	public void deletePlan(String planName) {
		if (PlanType.DEFAULT.name().equals(planName)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Invalid plan name");
		}
		
		accountService.getAccountsByPlanName(planName).forEach(account ->
			accountService.updateAccountPlan(account.getAdminId(), PlanType.DEFAULT.name()));
		
		planDao.deleteByName(planName);
	}

	public void deletePlanV2(String planName) {
		if (PlanType.DEFAULT.name().equals(planName)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Invalid plan name");
		}

		accountService.getAccountsByPlanV2Name(planName).forEach(account ->
				accountService.updateAccountPlanV2(account.getAdminId(), PlanType.DEFAULT.name()));

		planDao.deleteV2ByName(planName);
	}
	
	public List<Plan> listAll() {
		return planDao.getPlanRepository().findAll();
	}

	public List<PlanV2> listAllV2() {
		return planDao.getPlanV2Repository().findAll();
	}
	
	public Plan getPlanByName(String planName) {
		var plan = planDao.findByName(planName);
		
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		return plan;
	}

	public PlanV2 getPlanV2ByName(String planName) {
		var plan = planDao.findV2ByName(planName);

		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}

		return plan;
	}
	
}
