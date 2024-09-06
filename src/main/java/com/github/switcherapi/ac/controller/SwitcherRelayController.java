package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.config.SwitcherConfig;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.ValidatorService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.github.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

@RestController
@RequestMapping("switcher/v1")
public class SwitcherRelayController {
	
	private final Gson gson = new Gson();

	private final AccountService accountService;

	private final ValidatorFactory validatorFactory;

	private final ValidatorService validatorService;

	private final SwitcherConfig switcherConfig;
	
	public SwitcherRelayController(
			AccountService accountService, 
			ValidatorFactory validatorFactory,
			ValidatorService validatorService,
			SwitcherConfig switcherConfig) {
		this.accountService = accountService;
		this.validatorFactory = validatorFactory;
		this.validatorService = validatorService;
		this.switcherConfig = switcherConfig;
	}

	@GetMapping(value = "/verify")
	public ResponseEntity<Object> verify() {
		return ResponseEntity.ok(Map.of("code", switcherConfig.relayCode()));
	}

	@Operation(summary = "Load new account to Switcher AC")
	@PostMapping(value = "/create")
	public ResponseEntity<ResponseRelayDTO> loadAccount(@RequestBody RequestRelayDTO request) {
		try {
			accountService.createAccount(request.value());
			return ResponseEntity.ok(ResponseRelayDTO.create(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}

	@Operation(summary = "Remove existing account from Switcher AC")
	@PostMapping(value = "/remove")
	public ResponseEntity<ResponseRelayDTO> removeAccount(@RequestBody RequestRelayDTO request) {
		try {
			accountService.deleteAccount(request.value());
			return ResponseEntity.ok(ResponseRelayDTO.create(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}

	@Operation(summary = "Returns rate limit for API usage")
	@GetMapping(value = "/limiter")
	public ResponseEntity<ResponseRelayDTO> limiter(@RequestParam String value) {
		try {
			final var request = FeaturePayload.builder()
					.feature(RATE_LIMIT.getValue())
					.owner(value)
					.build();

			return ResponseEntity.ok(validatorFactory.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation given input value")
	@PostMapping(value = "/validate")
	public ResponseEntity<Object> validate(@RequestBody RequestRelayDTO request) {
		try {
			var featureRequest = gson.fromJson(request.payload(), FeaturePayload.class);
			return ResponseEntity.ok(validatorService.execute(featureRequest));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}

}
