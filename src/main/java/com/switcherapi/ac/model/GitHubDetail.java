package com.switcherapi.ac.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubDetail(
		String id,
		String name,
		String login,
		@JsonProperty("avatar_url")
		@SerializedName("avatar_url")
		String avatarUrl
) {
}
