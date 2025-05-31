package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.dto.AccountDTO;
import com.github.switcherapi.ac.model.dto.GitHubAuthDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
	public Mono<ResponseEntity<GitHubAuthDTO>> gitHubAuth(@RequestParam String code) {
		return adminService.gitHubAuth(code).map(ResponseEntity::ok);
	}
	
	@SecurityRequirements
	@Operation(summary = "Update JWT using your refresh token")
	@PostMapping(value = "/auth/refresh")
	public Mono<ResponseEntity<GitHubAuthDTO>> gitHubRefreshAuth(
			@RequestHeader(HttpHeaders.AUTHORIZATION) String token, @RequestParam String refreshToken) {
		return adminService.refreshToken(token, refreshToken).map(ResponseEntity::ok);
	}
	
	@PostMapping(value = "/logout")
	public Mono<ResponseEntity<Object>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
		return adminService.logout(token).map(ResponseEntity::ok);
	}
	
	@Operation(summary = "Update account plan with another plan")
	@PatchMapping(value = "/account/change/{adminId}")
	public Mono<ResponseEntity<AccountDTO>> changeAccountPlan(
			@PathVariable(value = "adminId") String adminId,
			@RequestParam String plan) {
		return accountService.createAccount(adminId, plan).map(ResponseEntity::ok);
	}

}
