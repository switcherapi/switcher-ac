package com.github.switcherac.service;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherac.model.Plan;
import com.github.switcherac.model.PlanType;
import com.github.switcherac.repository.PlanDao;

@Service
public class PlanService {
	
	private final String PLAN_NOT_FOUND = "Unable to find plan %s";
	
	@Autowired
	private PlanDao planDao;
	
	@Autowired
	private AccountService accountService;
	
	public Plan createPlan(Plan plan) {
		Plan newPlan = planDao.findByName(plan.getName());
		newPlan = newPlan != null ? newPlan : new Plan();
		loadAttributes(plan, newPlan);
		planDao.getPlanRepository().save(newPlan);
		return plan;
	}
	
	public Plan updatePlan(String planName, Plan plan) {
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
		
		accountService.getAccountsByPlanName(planName).forEach(account -> {
			accountService.updateAccountPlan(account.getAdminId(), PlanType.DEFAULT.name());
		});
		
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
	
	private void loadAttributes(Plan from, Plan to) {
		for (Field field : Plan.class.getDeclaredFields()) {
			try {
				field.setAccessible(true);
				if (field.get(from) != null) {
					field.setAccessible(true);
					field.set(to, field.get(from));
				}
			} catch (Exception e) {
				throw new ResponseStatusException(
						HttpStatus.INTERNAL_SERVER_ERROR, 
						"Something went wrong while attempting to load attributes", e);
			} finally {
				field.setAccessible(false);
			}
		}
	}

}
