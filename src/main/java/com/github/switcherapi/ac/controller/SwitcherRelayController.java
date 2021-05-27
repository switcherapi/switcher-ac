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

@RestController
@RequestMapping("switcher")
public class SwitcherRelayController {
	
	private AccountService accountService;
	
	private ValidatorFactory validatorFactory;
	
	public SwitcherRelayController(
			AccountService accountService, 
			ValidatorFactory validatorFactory) {
		this.accountService = accountService;
		this.validatorFactory = validatorFactory;
	}

	@PostMapping(value = "/v1/create")
	public ResponseEntity<ResponseRelay> loadAccount(@RequestBody RequestRelay request) {
		try {
			accountService.createAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

	@PostMapping(value = "/v1/remove")
	public ResponseEntity<ResponseRelay> removeAccount(@RequestBody RequestRelay request) {
		try {
			accountService.deleteAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@GetMapping(value = "/v1/execution")
	public ResponseEntity<ResponseRelay> execution(@RequestParam String value) {
		try {
			final var request = new RequestRelay();
			request.setValue(String.format("execution#%s", value));
			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@PostMapping(value = "/v1/validate")
	public ResponseEntity<Object> validate(@RequestBody RequestRelay request) {
		try {
			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelay(false, e.getMessage()));
		}
	}

}
