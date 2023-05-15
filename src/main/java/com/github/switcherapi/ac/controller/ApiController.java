package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.config.ConfigProperties;
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

	private final ConfigProperties configProperties;

	public ApiController(ConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	@SecurityRequirements
	@Operation(summary = "Check if API is running")
	@GetMapping(value = "/check")
	public ResponseEntity<Map<String, Object>> check() {
		return ResponseEntity.ok(Map.of(
				"status", "All good",
				"version", configProperties.getVersion(),
				"release_time", configProperties.getReleaseTime()
		));
	}

}
