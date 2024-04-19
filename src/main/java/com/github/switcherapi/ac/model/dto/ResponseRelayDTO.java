package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@JsonInclude(Include.NON_NULL)
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseRelayDTO {

	private boolean result;

	private String message;

	private Metadata metadata;

	public static ResponseRelayDTO create(boolean result) {
		var response = new ResponseRelayDTO();
		response.setResult(result);
		return response;
	}

	public ResponseRelayDTO withMessage(String message) {
		this.message = message;
		return this;
	}

	public ResponseRelayDTO withMetadata(Metadata metadata) {
		this.metadata = metadata;
		return this;
	}

}
