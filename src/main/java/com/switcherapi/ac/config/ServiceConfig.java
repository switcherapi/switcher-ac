package com.switcherapi.ac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service")
public record ServiceConfig(
		Endpoint endpoint,
		Docs docs
) {

	public record Endpoint(
		String health) { }

	public record Docs(
		String title,
		String description,
		String version,
		String releaseTime,
		String url,
		License license,
		Contact contact) {

		public record License(
			String type,
			String url) { }

		public record Contact(
			String author,
			String email) { }
	}
}
