package com.github.switcherapi.ac.model;

import org.springframework.data.mongodb.core.index.Indexed;

public class PlanDTO {
	
	@Indexed(unique = true)
	protected String name;
	
	protected Integer maxDomains;
	
	protected Integer maxGroups;
	
	protected Integer maxSwitchers;
	
	protected Integer maxComponents;
	
	protected Integer maxEnvironments;
	
	protected Integer maxTeams;
	
	protected Integer maxDailyExecution;
	
	protected Boolean enableMetrics;
	
	protected Boolean enableHistory;

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

	public Boolean isEnableMetrics() {
		return enableMetrics;
	}

	public void setEnableMetrics(Boolean enableMetrics) {
		this.enableMetrics = enableMetrics;
	}

	public Boolean isEnableHistory() {
		return enableHistory;
	}

	public void setEnableHistory(Boolean enableHistory) {
		this.enableHistory = enableHistory;
	}

}
