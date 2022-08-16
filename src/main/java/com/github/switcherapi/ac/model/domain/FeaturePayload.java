package com.github.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
@Builder
public class FeaturePayload {

	private String feature;
	
	private String owner;
	
	private Integer total;

}
