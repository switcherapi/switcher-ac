package com.github.switcherapi.ac.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Execution(ExecutionMode.CONCURRENT)
class ApiControllerTests {
	
	@Autowired WebTestClient webTestClient;
	
	@Test
	void shouldReturnAllGood() {
		webTestClient.get()
			.uri("/api/check")
			.exchange()
			.expectStatus().isOk()
			.expectBody(String.class)
			.value(response ->
					assertTrue(response.contains("All good"), "Response should contain 'All good'"));
	}

}
