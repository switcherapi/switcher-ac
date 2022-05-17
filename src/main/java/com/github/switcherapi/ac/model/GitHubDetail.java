package com.github.switcherapi.ac.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GitHubDetail {

	private String id;

	private String name;

	private String login;

	@JsonProperty("avatar_url")
	private String avatarUrl;

}
