package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("api")
public class ApiController {

	@RequestMapping(value = "/check")
	public ResponseEntity<String> check() {
		return ResponseEntity.ok("All good");
	}

}
