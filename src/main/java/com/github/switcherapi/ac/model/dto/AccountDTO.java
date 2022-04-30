package com.github.switcherapi.ac.model.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AccountDTO {
	
	private String id;
	
	private String adminId;
	
	private PlanDTO plan;
	
	private Date lastReset;
	
	private int currentDailyExecution;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAdminId() {
		return adminId;
	}

	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}

	public PlanDTO getPlan() {
		return plan;
	}

	public void setPlan(PlanDTO plan) {
		this.plan = plan;
	}
	
	public Date getLastReset() {
		return lastReset;
	}

	public void setLastReset(Date lastReset) {
		this.lastReset = lastReset;
	}

	public int getCurrentDailyExecution() {
		return currentDailyExecution;
	}

	public void setCurrentDailyExecution(int currentDailyExecution) {
		this.currentDailyExecution = currentDailyExecution;
	}
	
}
