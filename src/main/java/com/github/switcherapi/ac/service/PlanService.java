package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
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

	public PlanV2 createPlanV2(PlanV2 plan) {
		var newPlan = planDao.findV2ByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new PlanV2();

		PlanMapper.copyProperties(plan, newPlan);
		return planDao.getPlanV2Repository().save(newPlan);
	}

	public PlanV2 updatePlanV2(String planName, PlanV2 plan) {
		var planFound = getPlanV2ByName(planName);
		PlanMapper.copyProperties(plan, planFound);
		planDao.getPlanV2Repository().save(planFound);

		return planFound;
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

	public List<PlanV2> listAllV2() {
		return planDao.getPlanV2Repository().findAll();
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
