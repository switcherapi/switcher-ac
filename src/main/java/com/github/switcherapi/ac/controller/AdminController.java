package com.github.switcherapi.ac.controller;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

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

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.mapper.AccountMapper;
import com.github.switcherapi.ac.model.mapper.DefaultMapper;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.PlanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("admin/v1")
public class AdminController {
	
	private final PlanService planService;
	
	private final AccountService accountService;
	
	private final AdminService adminService;

	public AdminController(
			PlanService planService, 
			AccountService accountService, 
			AdminService adminService) {
		this.planService = planService;
		this.accountService = accountService;
		this.adminService = adminService;
	}

	@SecurityRequirements
	@Operation(summary = "Authenticate using GitHub credentials")
	@PostMapping(value = "/auth/github")
	public ResponseEntity<GitHubAuthDTO> gitHubAuth(@RequestParam String code) {
		return ResponseEntity.ok(adminService.gitHubAuth(code));
	}
	
	@SecurityRequirements
	@Operation(summary = "Update JWT using your refresh token")
	@PostMapping(value = "/auth/refresh")
	public ResponseEntity<GitHubAuthDTO> gitHubRefreshAuth(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestParam String refreshToken) {
		return ResponseEntity.ok(adminService.refreshToken(token, refreshToken));
	}
	
	@PostMapping(value = "/logout")
	public ResponseEntity<Object> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
		adminService.logout(token);
		return ResponseEntity.ok().build();
	}
	
	@Operation(summary = "Update account plan with another plan")
	@PatchMapping(value = "/account/change/{adminId}")
	public ResponseEntity<AccountDTO> changeAccountPlan(@PathVariable(value="adminId") 
		String adminId, @RequestParam String plan) {
		final var account = accountService.createAccount(adminId, plan);
		return ResponseEntity.ok(AccountMapper.createCopy(account));
	}
	
	@Operation(summary = "Reset execution credits")
	@PatchMapping(value = "/account/reset/{adminId}")
	public ResponseEntity<AccountDTO> changeAccountPlan(@PathVariable(value="adminId") String adminId) {
		final var account = accountService.resetDailyExecution(adminId);
		return ResponseEntity.ok(AccountMapper.createCopy(account));
	}
	
	@Operation(summary = "Create a new plan")
	@PostMapping(value = "/plan")
	public ResponseEntity<PlanDTO> createPlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		assert plan != null;
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.createPlan(plan), PlanDTO.class));
	}
	
	@Operation(summary = "Update existing plan")
	@PatchMapping(value = "/plan")
	public ResponseEntity<PlanDTO> updatePlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		assert plan != null;
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.updatePlan(plan.getName(), plan), PlanDTO.class));
	}
	
	@Operation(summary = "Delete existing plan")
	@DeleteMapping(value = "/plan")
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		planService.deletePlan(plan);
		return ResponseEntity.ok("Plan deleted");
	}
	
	@Operation(summary = "List available plans")
	@GetMapping(value = "/plan/list")
	public ResponseEntity<List<PlanDTO>> listPlans() {
		return ResponseEntity.ok(PlanMapper.createCopy(planService.listAll()));
	}
	
	@Operation(summary = "Return one plan")
	@GetMapping(value = "/plan/get")
	public ResponseEntity<PlanDTO> listPlans(@RequestParam String plan) {
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.getPlanByName(plan), PlanDTO.class));
	}
}
