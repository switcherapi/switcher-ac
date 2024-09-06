package com.github.switcherapi.ac.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubDetail(
		String id,
		String name,
		String login,
		@JsonProperty("avatar_url")
		String avatarUrl
) {
}
