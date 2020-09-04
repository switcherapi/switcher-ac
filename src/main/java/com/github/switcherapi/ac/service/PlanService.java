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

@Service
public class PlanService {
	
	private static final String PLAN_NOT_FOUND = "Unable to find plan %s";
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountService accountService;
	
	public Plan createPlan(PlanDTO plan) {
		Plan newPlan = planDao.findByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new Plan();
		loadAttributes(plan, newPlan);
		return planDao.getPlanRepository().save(newPlan);
	}
	
	public Plan updatePlan(String planName, PlanDTO plan) {
		Plan planFound = getPlanByName(planName);
		loadAttributes(plan, planFound);
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
		Plan plan = planDao.findByName(planName);
		
		if (plan == null) {
			throw new ResponseStatusException(
					HttpStatus.NOT_FOUND, String.format(PLAN_NOT_FOUND, planName));
		}
		
		return plan;
	}
	
	private void loadAttributes(PlanDTO from, Plan to) {
		to.setName(from.getName() != null ? from.getName() : to.getName());
		to.setMaxDomains(from.getMaxDomains() != null ? from.getMaxDomains() : to.getMaxDomains());
		to.setMaxGroups(from.getMaxGroups() != null ? from.getMaxGroups() : to.getMaxGroups());
		to.setMaxSwitchers(from.getMaxSwitchers() != null ? from.getMaxSwitchers() : to.getMaxSwitchers());
		to.setMaxComponents(from.getMaxComponents() != null ? from.getMaxComponents() : to.getMaxComponents());
		to.setMaxEnvironments(from.getMaxEnvironments() != null ? from.getMaxEnvironments() : to.getMaxEnvironments());
		to.setMaxDailyExecution(from.getMaxDailyExecution() != null ? from.getMaxDailyExecution() : to.getMaxDailyExecution());
		to.setMaxTeams(from.getMaxTeams() != null ? from.getMaxTeams() : to.getMaxTeams());
		to.setEnableHistory(from.isEnableHistory() != null ? from.isEnableHistory() : to.isEnableHistory());
		to.setEnableMetrics(from.isEnableMetrics() != null ? from.isEnableMetrics() : to.isEnableMetrics());
	}

}
