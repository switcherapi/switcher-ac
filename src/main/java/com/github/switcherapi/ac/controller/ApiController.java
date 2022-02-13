package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("api")
public class ApiController {

	@ApiOperation(value = "Check if API is running")
	@GetMapping(value = "/check")
	public ResponseEntity<String> check() {
		return ResponseEntity.ok("All good");
	}

}
