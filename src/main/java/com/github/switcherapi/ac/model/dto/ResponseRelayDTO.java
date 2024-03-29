package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.Generated;

@Generated
@JsonInclude(Include.NON_NULL)
@Data
public class ResponseRelayDTO {

	private boolean result;

	private String message;

	public ResponseRelayDTO(boolean result, String message) {
		this.result = result;
		this.message = message;
	}

	public ResponseRelayDTO(boolean result) {
		this(result, null);
	}

}
