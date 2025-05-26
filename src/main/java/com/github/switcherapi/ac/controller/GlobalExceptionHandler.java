package com.github.switcherapi.ac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", ex.getMessage());
		errorResponse.put("status", String.valueOf(ex.getStatusCode().value()));
		return new ResponseEntity<>(errorResponse, ex.getStatusCode());
	}
}