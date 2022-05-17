package com.github.switcherapi.ac.model.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.Generated;

@Generated
@JsonInclude(Include.NON_NULL)
@Document(collection = "plans")
@Data
public class Plan {

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private Integer maxDomains;

	private Integer maxGroups;

	private Integer maxSwitchers;

	private Integer maxComponents;

	private Integer maxEnvironments;

	private Integer maxTeams;

	private Integer maxDailyExecution;

	private Boolean enableMetrics;

	private Boolean enableHistory;

	public static Plan loadDefault() {
		var plan = new Plan();
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

}
