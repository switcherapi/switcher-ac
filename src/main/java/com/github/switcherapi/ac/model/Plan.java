package com.github.switcherapi.ac.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Document(collection = "plans")
public class Plan extends PlanDTO {
	
	@Id
	private String id;
	
	public static PlanDTO loadDefault() {
		var plan = new PlanDTO();
		plan.setName(PlanType.DEFAULT.name());
		plan.setMaxDomains(1);
		plan.setMaxGroups(2);
		plan.setMaxSwitchers(3);
		plan.setMaxEnvironments(2);
		plan.setMaxComponents(2);
		plan.setMaxTeams(1);
		plan.setMaxDailyExecution(100);
		plan.setEnableHistory(false);
		plan.setEnableMetrics(false);
		return plan;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
