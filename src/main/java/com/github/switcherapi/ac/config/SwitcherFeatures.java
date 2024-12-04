package com.github.switcherapi.ac.config;

import com.github.switcherapi.ac.util.FileUtil;
import com.github.switcherapi.client.SnapshotCallback;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherKey;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix = "switcher")
public class SwitcherFeatures extends SwitcherContextBase implements SnapshotCallback {

	@SwitcherKey
	public static final String SWITCHER_AC_ADM = "SWITCHER_AC_ADM";

	private String relayCode;

	@PostConstruct
	@Override
	protected void configureClient() {
		super.truststore.setPath(FileUtil.getFilePathFromResource(truststore.getPath()));
		super.configureClient();

		scheduleSnapshotAutoUpdate(snapshot.getUpdateInterval(), this);
		checkSwitchers();
	}

	@Override
	public void onSnapshotUpdate(long version) {
		log.info("Snapshot updated: {}", version);
	}

}
