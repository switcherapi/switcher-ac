package com.github.switcherapi.ac.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Document(collection = "admins")
public class Admin {
	
	@Id
	private String id;
	
	@JsonIgnore
	private String token;
	
	@Indexed(unique = true)
	private String gitHubId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getGitHubId() {
		return gitHubId;
	}

	public void setGitHubId(String gitHubId) {
		this.gitHubId = gitHubId;
	}

}
