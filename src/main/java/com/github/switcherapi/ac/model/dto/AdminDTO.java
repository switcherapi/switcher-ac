package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AdminDTO {
	
	private String id;
	
	private String gitHubId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGitHubId() {
		return gitHubId;
	}

	public void setGitHubId(String gitHubId) {
		this.gitHubId = gitHubId;
	}

}
