package com.github.switcherapi.ac.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Document(collection = "accounts")
public class Account {
	
	@Id
	private String id;
	
	@Indexed(unique = true)
	private String adminId;
	
	@DBRef
	private Plan plan;
	
	private Date lastReset = new Date();
	
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

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
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
