package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.github.switcherapi.ac.model.domain.Plan;

@JsonInclude(Include.NON_NULL)
public class PlanDTO extends Plan {
	
}
