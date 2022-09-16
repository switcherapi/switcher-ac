package com.github.switcherapi.ac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "service.docs")
@Getter
@Setter
public class ConfigProperties {

	private String title;
	private String description;
	private String version;
	private String url;
	private License license;
	private Contact contact;

	@Getter
	@Setter
	public static class License {

		private String type;
		private String url;

	}

	@Getter
	@Setter
	public static class Contact {

		private String author;
		private String email;

	}

}
