package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PlanDTO {
	
	private String id;

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
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaxDomains() {
		return maxDomains;
	}

	public void setMaxDomains(Integer maxDomains) {
		this.maxDomains = maxDomains;
	}

	public Integer getMaxGroups() {
		return maxGroups;
	}

	public void setMaxGroups(Integer maxGroups) {
		this.maxGroups = maxGroups;
	}

	public Integer getMaxSwitchers() {
		return maxSwitchers;
	}

	public void setMaxSwitchers(Integer maxSwitchers) {
		this.maxSwitchers = maxSwitchers;
	}

	public Integer getMaxComponents() {
		return maxComponents;
	}

	public void setMaxComponents(Integer maxComponents) {
		this.maxComponents = maxComponents;
	}

	public Integer getMaxEnvironments() {
		return maxEnvironments;
	}

	public void setMaxEnvironments(Integer maxEnvironments) {
		this.maxEnvironments = maxEnvironments;
	}

	public Integer getMaxTeams() {
		return maxTeams;
	}

	public void setMaxTeams(Integer maxTeams) {
		this.maxTeams = maxTeams;
	}

	public Integer getMaxDailyExecution() {
		return maxDailyExecution;
	}

	public void setMaxDailyExecution(Integer maxDailyExecution) {
		this.maxDailyExecution = maxDailyExecution;
	}

	public Boolean getEnableMetrics() {
		return enableMetrics;
	}

	public void setEnableMetrics(Boolean enableMetrics) {
		this.enableMetrics = enableMetrics;
	}

	public Boolean getEnableHistory() {
		return enableHistory;
	}

	public void setEnableHistory(Boolean enableHistory) {
		this.enableHistory = enableHistory;
	}

}
