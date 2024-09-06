package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.switcherapi.ac.model.domain.PlanAttribute;

import java.util.List;

@JsonInclude(Include.NON_NULL)
public record PlanDTO(
		String id,
		String name,
		List<PlanAttribute> attributes) {
}
