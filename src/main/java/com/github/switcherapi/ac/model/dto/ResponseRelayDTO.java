package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record ResponseRelayDTO(
		boolean result,
		String message,
		Metadata metadata) {

	public static ResponseRelayDTO create(boolean result) {
		return new ResponseRelayDTO(result, null, null);
	}

	public static ResponseRelayDTO fail(String message) {
		return new ResponseRelayDTO(false, message, null);
	}

	public static ResponseRelayDTO success(Metadata metadata) {
		return new ResponseRelayDTO(true, null, metadata);
	}

}
