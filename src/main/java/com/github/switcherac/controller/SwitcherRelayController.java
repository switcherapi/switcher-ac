package com.github.switcherac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.switcherac.model.request.RequestRelay;
import com.github.switcherac.model.response.ResponseRelay;
import com.github.switcherac.service.AccountControlService;
import com.github.switcherac.service.AccountService;

@CrossOrigin
@RestController
@RequestMapping("switcher")
public class SwitcherRelayController {
	
	private final String VALIDATE_SEPARATOR = "#";
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountControlService accountControlService;
	
	@RequestMapping(value = "/v1/create", method = RequestMethod.POST)
	public ResponseEntity<ResponseRelay> loadAccount(@RequestBody RequestRelay request) {
		try {
			accountService.createAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

	@RequestMapping(value = "/v1/remove", method = RequestMethod.POST)
	public ResponseEntity<ResponseRelay> removeAccount(@RequestBody RequestRelay request) {
		try {
			accountService.deleteAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/v1/execution", method = RequestMethod.GET)
	public ResponseEntity<ResponseRelay> execution(@RequestParam String value) {
		try {
			return ResponseEntity.ok(
					accountControlService.checkExecution(value));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}
	
	@RequestMapping(value = "/v1/validate", method = RequestMethod.POST)
	public ResponseEntity<ResponseRelay> validate(@RequestBody RequestRelay request) {
		try {
			final String[] args = request.getValue().split(VALIDATE_SEPARATOR);
			
			switch (args[0]) {
			case "domain":
				return ResponseEntity.ok(
						accountControlService.checkDomain(
								args[1], Integer.parseInt(request.getNumeric())));
			case "group":
				return ResponseEntity.ok(
						accountControlService.checkGroup(
								args[1], Integer.parseInt(request.getNumeric())));
			case "switcher":
				return ResponseEntity.ok(
						accountControlService.checkSwitcher(
								args[1], Integer.parseInt(request.getNumeric())));
			case "component":
				return ResponseEntity.ok(
						accountControlService.checkComponent(
								args[1], Integer.parseInt(request.getNumeric())));
			case "environment":
				return ResponseEntity.ok(
						accountControlService.checkEnvironment(
								args[1], Integer.parseInt(request.getNumeric())));
			case "team":
				return ResponseEntity.ok(
						accountControlService.checkTeam(
								args[1], Integer.parseInt(request.getNumeric())));
			case "metrics":
				return ResponseEntity.ok(
						accountControlService.checkMetrics(args[1]));
			case "history":
				return ResponseEntity.ok(
						accountControlService.checkHistory(args[1]));
			default:
				return ResponseEntity.ok(
						new ResponseRelay(false, 
								String.format(
										"Invalid arguments - value %s - numeric %s", 
										request.getValue(), request.getNumeric())));
			}

		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

}
