package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonInclude(Include.NON_NULL)
@Data
public class AdminDTO {

	private String id;

	private String gitHubId;

}
