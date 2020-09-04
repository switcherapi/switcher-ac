package com.github.switcherapi.ac.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;

@CrossOrigin
@RestController
@RequestMapping("admin")
public class AdminController {
	
	@Autowired
	private PlanService planService;
	
	@Autowired
	private AccountService accountService;
	
	@PatchMapping(value = "/account/v1/change/{adminId}")
	public ResponseEntity<Account> changeAccountPlan(@PathVariable(value="adminId") 
		String adminId, @RequestParam String plan) {
		return ResponseEntity.ok(accountService.createAccount(adminId, plan));
	}
	
	@PatchMapping(value = "/account/v1/reset/{adminId}")
	public ResponseEntity<Account> changeAccountPlan(@PathVariable(value="adminId") String adminId) {
		return ResponseEntity.ok(accountService.resetDailyExecution(adminId));
	}
	
	@PostMapping(value = "/plan/v1")
	public ResponseEntity<Plan> createPlan(@RequestBody PlanDTO plan) {
		return ResponseEntity.ok(planService.createPlan(plan));
	}
	
	@PatchMapping(value = "/plan/v1")
	public ResponseEntity<Plan> updatePlan(@RequestBody PlanDTO plan) {
		return ResponseEntity.ok(planService.updatePlan(plan.getName(), plan));
	}
	
	@DeleteMapping(value = "/plan/v1")
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		planService.deletePlan(plan);
		return ResponseEntity.ok("Plan deleted");
	}
	
	@GetMapping(value = "/plan/v1/list")
	public ResponseEntity<List<Plan>> listPlans() {
		return ResponseEntity.ok(planService.listAll());
	}
	
	@GetMapping(value = "/plan/v1/get")
	public ResponseEntity<Plan> listPlans(@RequestParam String plan) {
		return ResponseEntity.ok(planService.getPlanByName(plan));
	}
}
