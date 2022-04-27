package com.github.switcherapi.ac.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.switcherapi.ac.model.Account;
import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("admin/v1")
public class AdminController {
	
	private PlanService planService;
	
	private AccountService accountService;
	
	private AdminService adminService;
	
	public AdminController(
			PlanService planService, 
			AccountService accountService, 
			AdminService adminService) {
		this.planService = planService;
		this.accountService = accountService;
		this.adminService = adminService;
	}

	@Operation(summary = "Authenticate using GitHub credentials")
	@PostMapping(value = "/auth/github")
	public ResponseEntity<Map<String, Object>> gitHubAuth(@RequestParam String code) {
		return ResponseEntity.ok(adminService.gitHubAuth(code));
	}
	
	@Operation(summary = "Update JWT using your refresh token")
	@PostMapping(value = "/auth/refresh")
	public ResponseEntity<Map<String, Object>> gitHubRefreshAuth(
			@RequestHeader("Authorization") String token, @RequestParam String refreshToken) {
		return ResponseEntity.ok(adminService.refreshToken(token, refreshToken));
	}
	
	@PostMapping(value = "/logout")
	public ResponseEntity<Object> logout(@RequestHeader("Authorization") String token) {
		adminService.logout(token);
		return ResponseEntity.ok().build();
	}
	
	@Operation(summary = "Update account plan with another plan")
	@PatchMapping(value = "/account/change/{adminId}")
	public ResponseEntity<Account> changeAccountPlan(@PathVariable(value="adminId") 
		String adminId, @RequestParam String plan) {
		return ResponseEntity.ok(accountService.createAccount(adminId, plan));
	}
	
	@Operation(summary = "Reset execution credits")
	@PatchMapping(value = "/account/reset/{adminId}")
	public ResponseEntity<Account> changeAccountPlan(@PathVariable(value="adminId") String adminId) {
		return ResponseEntity.ok(accountService.resetDailyExecution(adminId));
	}
	
	@Operation(summary = "Create a new plan")
	@PostMapping(value = "/plan")
	public ResponseEntity<Plan> createPlan(@RequestBody PlanDTO plan) {
		return ResponseEntity.ok(planService.createPlan(plan));
	}
	
	@Operation(summary = "Update existing plan")
	@PatchMapping(value = "/plan")
	public ResponseEntity<Plan> updatePlan(@RequestBody PlanDTO plan) {
		return ResponseEntity.ok(planService.updatePlan(plan.getName(), plan));
	}
	
	@Operation(summary = "Delete existing plan")
	@DeleteMapping(value = "/plan")
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		planService.deletePlan(plan);
		return ResponseEntity.ok("Plan deleted");
	}
	
	@Operation(summary = "List available plans")
	@GetMapping(value = "/plan/list")
	public ResponseEntity<List<Plan>> listPlans() {
		return ResponseEntity.ok(planService.listAll());
	}
	
	@Operation(summary = "Return one plan")
	@GetMapping(value = "/plan/get")
	public ResponseEntity<Plan> listPlans(@RequestParam String plan) {
		return ResponseEntity.ok(planService.getPlanByName(plan));
	}
}
