package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("switcher/v1")
public class SwitcherRelayController {
	
	private AccountService accountService;
	
	private ValidatorFactory validatorFactory;
	
	public SwitcherRelayController(
			AccountService accountService, 
			ValidatorFactory validatorFactory) {
		this.accountService = accountService;
		this.validatorFactory = validatorFactory;
	}

	@Operation(summary = "Load new account to Switcher AC")
	@PostMapping(value = "/create")
	public ResponseEntity<ResponseRelay> loadAccount(@RequestBody RequestRelay request) {
		try {
			accountService.createAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

	@Operation(summary = "Remove existing account from Switcher AC")
	@PostMapping(value = "/remove")
	public ResponseEntity<ResponseRelay> removeAccount(@RequestBody RequestRelay request) {
		try {
			accountService.deleteAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation on execution credits")
	@GetMapping(value = "/execution")
	public ResponseEntity<ResponseRelay> execution(@RequestParam String value) {
		try {
			final var request = new RequestRelay();
			request.setValue(String.format("execution#%s", value));
			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation given input value")
	@PostMapping(value = "/validate")
	public ResponseEntity<Object> validate(@RequestBody RequestRelay request) {
		try {
			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelay(false, e.getMessage()));
		}
	}

}
