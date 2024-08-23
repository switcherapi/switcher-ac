package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.util.FileUtil;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SnapshotCallback;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.github.switcherapi.ac.config.SwitcherFeatures.configure;
import static com.github.switcherapi.ac.config.SwitcherFeatures.initializeClient;
import static com.github.switcherapi.ac.config.SwitcherFeatures.scheduleSnapshotAutoUpdate;

@Slf4j
@ConfigurationProperties(prefix = "switcher")
public record SwitcherConfig(
		String url,
		String apikey,
		String domain,
		String component,
		String environment,
		boolean local,
		String silent,
		SnapshotConfig snapshot,
		String relayCode,
		TruststoreConfig truststore) implements SnapshotCallback {

	record SnapshotConfig(
		String autoUpdateInterval,
		String location,
		boolean auto) { }

	record TruststoreConfig(
		String path,
		String password) { }

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
				.snapshotLocation(StringUtils.isNotBlank(snapshot.location()) ? snapshot.location() : null)
				.snapshotAutoLoad(snapshot.auto())
				.truststorePath(StringUtils.isNotBlank(truststore.path()) ? FileUtil.getFilePathFromResource(truststore.path()) : null)
				.truststorePassword(StringUtils.isNotBlank(truststore.password()) ? truststore.password() : null));

		initializeClient();
		scheduleSnapshotAutoUpdate(snapshot.autoUpdateInterval(), this);
	}

	@Override
	public void onSnapshotUpdate(long version) {
		log.info("Snapshot updated: {}", version);
	}

}
