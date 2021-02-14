package com.github.switcherapi.ac.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.github.switcherapi.ac.service.AccountControlService;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.validator.ValidatorFactory;

@RestController
@RequestMapping("switcher")
public class SwitcherRelayController {
	
	private static final String SEPARATOR = "#";
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountControlService accountControlService;
	
	@Autowired
	private ValidatorFactory validatorInitializer;
	
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
			return ResponseEntity.ok(
					accountControlService.checkExecution(value));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@PostMapping(value = "/v1/validate")
	public ResponseEntity<Object> validate(@RequestBody RequestRelay request) {
		try {
			final String[] args = request.getValue().split(SEPARATOR);
			if (request.getNumeric() != null) {
				return ResponseEntity.ok(validatorInitializer.runValidator(
						args[0], args[1], Integer.parseInt(request.getNumeric())));
			}
			
			return ResponseEntity.ok(validatorInitializer.runValidator(args[0], args[1]));
		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatus()).body(new ResponseRelay(false, e.getReason()));
		} catch (Exception e) {
			if (e.getCause() instanceof ResponseStatusException) {
				ResponseStatusException responseException = (ResponseStatusException) e.getCause();
				return ResponseEntity.status(responseException.getStatus()).body(new ResponseRelay(false, responseException.getMessage()));
			}
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

}
