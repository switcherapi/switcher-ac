package com.github.switcherapi.ac.config;

import com.github.switcherapi.client.ContextBuilder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

import static com.github.switcherapi.ac.config.SwitcherFeatures.configure;
import static com.github.switcherapi.ac.config.SwitcherFeatures.initializeClient;

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
	private TruststoreConfig truststore;
	
	@Data
	static class SnapshotConfig {
		private String location;
		private boolean auto;
	}

	@Data
	static class TruststoreConfig {
		private String path;
		private String password;
	}
	
	@Bean
	public void configureSwitcher() throws IOException {
		var truststorePath = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(truststore.getPath())) {
			truststorePath = ResourceUtils.getFile(truststore.getPath()).getAbsoluteFile().getPath();
		}

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
				.snapshotAutoLoad(snapshot.isAuto())
				.truststorePath(truststorePath)
				.truststorePassword(truststore.getPassword())
		);

		initializeClient();
	}

}
