package com.github.switcherac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherac.model.Plan;
import com.github.switcherac.service.PlanService;

@CrossOrigin
@RestController
@RequestMapping("admin")
public class AdminController {
	
	@Autowired
	private PlanService planService;
	
	@RequestMapping(value = "/plan/v1", method = RequestMethod.POST)
	public ResponseEntity<Plan> createPlan(@RequestBody Plan plan) {
		try {
			planService.createPlan(plan);
			return ResponseEntity.ok(plan);
		} catch (ResponseStatusException e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/plan/v1", method = RequestMethod.PATCH)
	public ResponseEntity<Plan> updatePlan(@RequestBody Plan plan) {
		try {
			planService.updatePlan(plan.getName(), plan);
			return ResponseEntity.ok(plan);
		} catch (ResponseStatusException e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/plan/v1", method = RequestMethod.DELETE)
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		try {
			planService.deletePlan(plan);
			return ResponseEntity.ok("Plan deleted");
		} catch (ResponseStatusException e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/plan/v1/list", method = RequestMethod.GET)
	public ResponseEntity<List<Plan>> listPlans() {
		try {
			return ResponseEntity.ok(planService.listAll());
		} catch (ResponseStatusException e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/plan/v1/get", method = RequestMethod.GET)
	public ResponseEntity<Plan> listPlans(@RequestParam String plan) {
		try {
			return ResponseEntity.ok(planService.getPlanByName(plan));
		} catch (ResponseStatusException e) {
			throw e;
		}
	}
}
