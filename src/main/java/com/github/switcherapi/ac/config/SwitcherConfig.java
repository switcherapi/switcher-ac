package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.util.FileUtil;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SnapshotCallback;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.github.switcherapi.ac.config.SwitcherFeatures.configure;
import static com.github.switcherapi.ac.config.SwitcherFeatures.initializeClient;
import static com.github.switcherapi.client.SwitcherContextBase.scheduleSnapshotAutoUpdate;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "switcher")
@Data
public class SwitcherConfig {

	private String url;
	private String apikey;
	private String domain;
	private String component;
	private String environment;
	private boolean local;
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
				.local(local)
				.silentMode(silent)
				.snapshotLocation(StringUtils.isNotBlank(snapshot.getLocation()) ? snapshot.getLocation() : null)
				.snapshotAutoLoad(snapshot.isAuto())
				.truststorePath(StringUtils.isNotBlank(truststore.getPath()) ? FileUtil.getFilePathFromResource(truststore.getPath()) : null)
				.truststorePassword(StringUtils.isNotBlank(truststore.getPassword()) ? truststore.getPassword() : null)
		);

		scheduleSnapshotAutoUpdate(snapshot.getAutoUpdateInterval(), new SnapshotCallback() {
			@Override
			public void onSnapshotUpdate(long version) {
				log.info("Snapshot updated: {}", version);
			}
		});

		initializeClient();
	}

}
