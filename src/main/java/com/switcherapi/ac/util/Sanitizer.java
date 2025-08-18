package com.switcherapi.ac.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

@UtilityClass
public class Sanitizer {

	public static String sanitize(String value, List<UnaryOperator<String>> sanitizers) {
		if (Objects.isNull(value)) {
			return StringUtils.EMPTY;
		}

		var sanitized = value;
		for (UnaryOperator<String> sanitizer : sanitizers) {
			sanitized = sanitizer.apply(sanitized);
		}

		return sanitized;
	}

	public static UnaryOperator<String> trim() {
		return String::trim;
	}

	public static UnaryOperator<String> alphaNumeric() {
		return alphaNumeric(StringUtils.EMPTY);
	}

	public static UnaryOperator<String> alphaNumeric(String... exceptions) {
		return value -> value.replaceAll("[^a-zA-Z0-9" + String.join(StringUtils.EMPTY, exceptions) + "]", StringUtils.EMPTY);
	}
}
