package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
import com.github.switcherapi.ac.repository.PlanDao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;

@Service
public class PlanService {

	private final PlanDao planDao;

	private final AccountService accountService;

	public PlanService(PlanDao planDao, AccountService accountService) {
		this.planDao = planDao;
		this.accountService = accountService;
	}

	public Plan createPlan(Plan plan) {
		var newPlan = planDao.findByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new Plan();

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
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Invalid plan name");
		}

		accountService.getAccountsByPlanName(planName).forEach(account ->
				accountService.updateAccountPlan(account.getAdminId(), PlanType.DEFAULT.name()));

		planDao.deleteByName(planName);
	}

	public List<Plan> listAll() {
		return planDao.getPlanRepository().findAll();
	}

	public Plan getPlanByName(String planName) {
		var plan = planDao.findByName(planName);

		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NAME_NOT_FOUND.getValue(), planName));
		}

		return plan;
	}
	
}
