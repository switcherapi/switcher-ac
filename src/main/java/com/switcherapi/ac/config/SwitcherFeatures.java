package com.switcherapi.ac.config;

import com.switcherapi.ac.util.FileUtil;
import com.switcherapi.client.SnapshotCallback;
import com.switcherapi.client.SwitcherContextBase;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@ConfigurationProperties(prefix = "switcher")
public class SwitcherFeatures extends SwitcherContextBase implements SnapshotCallback {

	public static final String SWITCHER_AC_ADM = "SWITCHER_AC_ADM";

	@Getter
	@Setter
	private String relayCode;

	@PostConstruct
	@Override
	protected void configureClient() {
		super.truststore.setPath(FileUtil.getFilePathFromResource(truststore.getPath()));
		super.registerSwitcherKeys(SWITCHER_AC_ADM);
		super.configureClient();

		scheduleSnapshotAutoUpdate(snapshot.getUpdateInterval(), this);
	}

	@Override
	public void onSnapshotUpdate(long version) {
		log.info("Snapshot updated: {}", version);
	}

}
