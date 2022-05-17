package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("api")
public class ApiController {

	@SecurityRequirements
	@Operation(summary = "Check if API is running")
	@GetMapping(value = "/check")
	public ResponseEntity<String> check() {
		return ResponseEntity.ok("All good");
	}

}
