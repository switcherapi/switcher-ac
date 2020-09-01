package com.github.switcherac.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.github.switcherac.model.request.RequestRelay;
import com.github.switcherac.model.response.ResponseRelay;
import com.github.switcherac.service.AccountService;

@CrossOrigin
@RestController
@RequestMapping("switcher")
public class SwitcherRelayController {
	
	@Autowired
	private AccountService accountService;
	
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ResponseEntity<ResponseRelay> loadAccount(RequestRelay request) {
		try {
			accountService.createAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	public ResponseEntity<ResponseRelay> removeAccount(RequestRelay request) {
		try {
			accountService.deleteAccount(request.getValue());
			return ResponseEntity.ok(new ResponseRelay(true));
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ResponseRelay(false, e.getMessage()));
		}
	}

}
