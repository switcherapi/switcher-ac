package com.github.switcherapi.ac.config;

import static com.github.switcherapi.ac.config.SwitcherFeatures.*;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.switcherapi.client.ContextBuilder;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "switcher")
@Data
public class SwitcherConfig {

	private String url;
	private String apikey;
	private String domain;
	private String component;
	private String environment;
	private boolean offline;
	private boolean silent;
	private String retry;
	private SnapshotConfig snapshot;
	private String relayCode;
	
	@Data
	static class SnapshotConfig {
		private String location;
		private boolean auto;
	}
	
	@Bean
	public void configureSwitcher() {
		configure(ContextBuilder.builder()
				.contextLocation(SwitcherFeatures.class.getName())
				.url(url)
				.apiKey(apikey)
				.domain(domain)
				.environment(environment)
				.component(component)
				.offlineMode(offline)
				.silentMode(silent)
				.retryAfter(retry)
				.snapshotLocation(snapshot.getLocation())
				.snapshotAutoLoad(snapshot.isAuto()));
		
		initializeClient();
	}

}
