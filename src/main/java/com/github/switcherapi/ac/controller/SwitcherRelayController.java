package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.config.SwitcherFeatures;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.ValidatorBasicService;
import com.github.switcherapi.ac.service.validator.ValidatorBuilderService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.github.switcherapi.ac.model.domain.Feature.RATE_LIMIT;

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
	public Mono<ResponseEntity<ResponseRelayDTO>> loadAccount(@RequestBody RequestRelayDTO request) {
		return accountService.createAccount(request.value())
				.map(account -> ResponseEntity.ok(ResponseRelayDTO.create(true)))
				.onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage()))));
	}

	@Operation(summary = "Remove existing account from Switcher AC")
	@PostMapping(value = "/remove")
	public Mono<ResponseEntity<ResponseRelayDTO>> removeAccount(@RequestBody RequestRelayDTO request) {
		return accountService.deleteAccount(request.value())
				.then(Mono.just(ResponseEntity.ok(ResponseRelayDTO.create(true))))
				.onErrorResume(e -> Mono.just(ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage()))));
	}

	@Operation(summary = "Returns rate limit for API usage")
	@GetMapping(value = "/limiter")
	@Cacheable(value = "limiterCache", key = "#value")
	public Mono<ResponseEntity<ResponseRelayDTO>> limiter(@RequestParam String value) {
		final var request = FeaturePayload.builder()
				.feature(RATE_LIMIT.getValue())
				.owner(value)
				.build();

		return validatorBuilderService.runValidator(request)
				.map(ResponseEntity::ok)
				.onErrorResume(ResponseStatusException.class, e ->
						Mono.just(ResponseEntity.status(e.getStatusCode())
								.body(ResponseRelayDTO.fail(e.getMessage())))
				);
	}
	
	@Operation(summary = "Perform account validation given input value")
	@PostMapping(value = "/validate")
	public Mono<ResponseEntity<ResponseRelayDTO>> validate(@RequestBody RequestRelayDTO request) {
		try {
			var featureRequest = gson.fromJson(request.payload().toString(), FeaturePayload.class);
			return validatorBasicService.execute(featureRequest)
					.map(ResponseEntity::ok)
					.onErrorResume(ResponseStatusException.class, e ->
							Mono.just(ResponseEntity.status(e.getStatusCode())
									.body(ResponseRelayDTO.fail(e.getMessage())))
					);
		} catch (ResponseStatusException e) {
			return Mono.just(ResponseEntity.status(e.getStatusCode()).body(ResponseRelayDTO.fail(e.getMessage())));
		} catch (Exception e) {
			return Mono.just(ResponseEntity.status(500).body(ResponseRelayDTO.fail(e.getMessage())));
		}
	}

}
