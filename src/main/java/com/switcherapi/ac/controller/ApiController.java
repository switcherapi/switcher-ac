package com.switcherapi.ac.controller;

import com.switcherapi.ac.config.ServiceConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api")
public class ApiController {

	private final ServiceConfig.Docs docs;

	public ApiController(ServiceConfig serviceConfig) {
		this.docs = serviceConfig.docs();
	}

	@SecurityRequirements
	@Operation(summary = "Check if API is running")
	@GetMapping(value = "/check")
	public ResponseEntity<Map<String, Object>> check() {
		return ResponseEntity.ok(Map.of(
				"status", "All good",
				"version", docs.version(),
				"release_time", docs.releaseTime()
		));
	}

}
