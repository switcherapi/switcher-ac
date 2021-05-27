package com.github.switcherapi.ac.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.repository.PlanDao;
import com.github.switcherapi.ac.service.util.PlanUtils;

@Service
public class PlanService {
	
	private static final String PLAN_NOT_FOUND = "Unable to find plan %s";
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountService accountService;

	public PlanService(PlanDao planDao, AccountService accountService) {
		super();
		this.planDao = planDao;
		this.accountService = accountService;
	}

	public Plan createPlan(PlanDTO plan) {
		var newPlan = planDao.findByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new Plan();
		
		PlanUtils.loadAttributes(plan, newPlan);
		return planDao.getPlanRepository().save(newPlan);
	}
	
	public Plan updatePlan(String planName, PlanDTO plan) {
		var planFound = getPlanByName(planName);
		PlanUtils.loadAttributes(plan, planFound);
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
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		return plan;
	}
	
}
