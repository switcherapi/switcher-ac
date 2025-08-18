package com.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record RequestRelayDTO(
		String value,
		Object payload) {
}
