package com.switcherapi.ac.controller;

import com.switcherapi.ac.model.dto.AccountDTO;
import com.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.switcherapi.ac.service.AccountService;
import com.switcherapi.ac.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/v1")
public class AdminController {
	
	private final AccountService accountService;
	
	private final AdminService adminService;

	public AdminController(
			AccountService accountService, 
			AdminService adminService) {
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
		return ResponseEntity.ok(accountService.createAccount(adminId, plan));
	}

}
