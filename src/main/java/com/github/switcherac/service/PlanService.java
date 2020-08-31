package com.github.switcherac.service;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.switcherac.model.Plan;
import com.github.switcherac.repository.PlanDao;
import com.github.switcherac.service.exception.ServiceException;

@Service
public class PlanService {
	
	@Autowired
	private PlanDao planDao;
	
	public void createPlan(String name) {
		final Plan plan = new Plan();
		plan.setName(name);
		planDao.getPlanRepository().save(plan);
	}
	
	public Plan updatePlan(String id, Plan plan) throws ServiceException {
		Optional<Plan> result = planDao.getPlanRepository().findById(id);
		
		Plan planFound = null;
		if (result.isPresent()) {
			planFound = result.get();
			
			for (Field field : Plan.class.getFields()) {
				try {
					if (field.get(plan) != null) {
						field.setAccessible(true);
						field.set(planFound, field.get(plan));
					}
				} catch (Exception e) {
					throw new ServiceException(
							String.format("Something went wrong while attempting to update %s", id), e);
				}
			}
		}
		
		return planFound;
	}

}
