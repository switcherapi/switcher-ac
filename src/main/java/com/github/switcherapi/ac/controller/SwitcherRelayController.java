package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;
import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("switcher/v1")
public class SwitcherRelayController {
	
	private final Gson gson = new Gson();

	private final AccountService accountService;
	
	private final ValidatorFactory validatorFactory;
	
	public SwitcherRelayController(
			AccountService accountService, 
			ValidatorFactory validatorFactory) {
		this.accountService = accountService;
		this.validatorFactory = validatorFactory;
	}

	@Operation(summary = "Load new account to Switcher AC")
	@PostMapping(value = "/create")
	public ResponseEntity<ResponseRelayDTO> loadAccount(@RequestBody RequestRelayDTO request) {
		try {
			accountService.createAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelayDTO(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelayDTO(false, e.getMessage()));
		}
	}

	@Operation(summary = "Remove existing account from Switcher AC")
	@PostMapping(value = "/remove")
	public ResponseEntity<ResponseRelayDTO> removeAccount(@RequestBody RequestRelayDTO request) {
		try {
			accountService.deleteAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelayDTO(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelayDTO(false, e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation on execution credits")
	@GetMapping(value = "/execution")
	public ResponseEntity<ResponseRelayDTO> execution(@RequestParam String value) {
		try {
			final var request = new FeaturePayload();
			request.setFeature("execution");
			request.setOwner(value);
			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelayDTO(false, e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation given input value")
	@PostMapping(value = "/validate")
	public ResponseEntity<Object> validate(@RequestBody RequestRelayDTO request) {
		try {
			var featureRequest = gson.fromJson(request.getPayload(), FeaturePayload.class);
			return ResponseEntity.ok(validatorFactory.runValidator(featureRequest));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelayDTO(false, e.getMessage()));
		}
	}

}
