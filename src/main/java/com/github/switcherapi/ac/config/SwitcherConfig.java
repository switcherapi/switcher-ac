package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.util.FileUtil;
import com.github.switcherapi.client.ContextBuilder;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
	private String silent;
	private SnapshotConfig snapshot;
	private String relayCode;
	private TruststoreConfig truststore;
	
	@Data
	static class SnapshotConfig {
		private String autoUpdateInterval;
		private String location;
		private boolean auto;
	}

	@Data
	static class TruststoreConfig {
		private String path;
		private String password;
	}
	
	@PostConstruct
	private void configureSwitcher() {
		configure(ContextBuilder.builder()
				.contextLocation(SwitcherFeatures.class.getName())
				.url(url)
				.apiKey(apikey)
				.domain(domain)
				.environment(environment)
				.component(component)
				.offlineMode(offline)
				.silentMode(silent)
				.snapshotAutoUpdateInterval(snapshot.getAutoUpdateInterval())
				.snapshotLocation(snapshot.getLocation())
				.snapshotAutoLoad(snapshot.isAuto())
				.truststorePath(FileUtil.getFilePathFromResource(truststore.getPath()))
				.truststorePassword(truststore.getPassword())
		);

		initializeClient();
	}

}
