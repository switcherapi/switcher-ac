package com.switcherapi.ac.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.List;

import static com.switcherapi.ac.util.Sanitizer.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
class SanitizerTest {

	@Test
	void shouldSanitizeStringTrim() {
		// Given
		var value = "  test  ";

		// When
		var sanitized = sanitize(value, List.of(trim()));

		// Then
		assertEquals("test", sanitized);
	}

	@Test
	void shouldSanitizeStringAlphaNumeric() {
		// Given
		var value = "test@123";

		// When
		var sanitized = sanitize(value, List.of(alphaNumeric()));

		// Then
		assertEquals("test123", sanitized);
	}

	@Test
	void shouldSanitizeStringTrimAndAlphaNumeric() {
		// Given
		var value = "  test@123  ";

		// When
		var sanitized = sanitize(value, List.of(trim(), alphaNumeric()));

		// Then
		assertEquals("test123", sanitized);
	}

	@Test
	void shouldSanitizerGitHubTokenPattern() {
		// Given
		var value = "gho_tH1s1s4T0k3n";

		// When
		var sanitized = sanitize(value, List.of(trim(), alphaNumeric("_")));

		// Then
		assertEquals("gho_tH1s1s4T0k3n", sanitized);
	}

	@Test
	void shouldSanitizeNull() {
		var sanitized = sanitize(null, List.of(trim(), alphaNumeric()));
		assertEquals(StringUtils.EMPTY, sanitized);
	}
}
