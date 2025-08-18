package com.switcherapi.ac.controller;

import com.switcherapi.ac.config.SwitcherFeatures;
import com.switcherapi.ac.model.domain.FeaturePayload;
import com.switcherapi.ac.model.dto.RequestRelayDTO;
import com.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.switcherapi.ac.service.AccountService;
import com.switcherapi.ac.service.ValidatorBasicService;
import com.switcherapi.ac.service.validator.ValidatorBuilderService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

@RestController
@RequestMapping("switcher/v1")
public class SwitcherRelayController {
	
	private final Gson gson = new Gson();

	private final AccountService accountService;

	private final ValidatorBuilderService validatorBuilderService;

	private final ValidatorBasicService validatorBasicService;

	private final SwitcherFeatures switcherConfig;
	
	public SwitcherRelayController(
			AccountService accountService,
			ValidatorBuilderService validatorBuilderService,
			ValidatorBasicService validatorBasicService,
			SwitcherFeatures switcherConfig) {
		this.accountService = accountService;
		this.validatorBuilderService = validatorBuilderService;
		this.validatorBasicService = validatorBasicService;
		this.switcherConfig = switcherConfig;
	}

	@GetMapping(value = "/verify")
	public ResponseEntity<Object> verify() {
		return ResponseEntity.ok(Map.of("code", switcherConfig.getRelayCode()));
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
	@Cacheable(value = "limiterCache", key = "#value")
	public ResponseEntity<ResponseRelayDTO> limiter(@RequestParam String value) {
		try {
			final var request = FeaturePayload.builder()
					.feature(RATE_LIMIT.getValue())
					.owner(value)
					.build();

			return ResponseEntity.ok(validatorBuilderService.runValidator(request));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}
	
	@Operation(summary = "Perform account validation given input value")
	@PostMapping(value = "/validate")
	@Cacheable(value = "validateCache", key = "#request.payload().toString()")
	public ResponseEntity<Object> validate(@RequestBody RequestRelayDTO request) {
		try {
			var featureRequest = gson.fromJson(String.valueOf(request.payload()), FeaturePayload.class);
			return ResponseEntity.ok(validatorBasicService.execute(featureRequest));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(ResponseRelayDTO.fail(e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage()));
		}
	}

}
