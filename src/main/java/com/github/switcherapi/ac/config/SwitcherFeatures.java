package com.github.switcherapi.ac.config;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherKey;

public class SwitcherFeatures extends SwitcherContextBase {
	
	@SwitcherKey
	public static final String SWITCHER_AC_ADM = "SWITCHER_AC_ADM";

	@SwitcherKey
	public static final String SWITCHER_AC_METADATA = "SWITCHER_AC_METADATA";

}
