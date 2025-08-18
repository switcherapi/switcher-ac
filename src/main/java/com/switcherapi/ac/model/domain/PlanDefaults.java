package com.switcherapi.ac.model.domain;

import lombok.Getter;

@Getter
public enum PlanDefaults {
	DEFAULT_DOMAIN(1),
	DEFAULT_GROUP(2),
	DEFAULT_SWITCHER(3),
	DEFAULT_ENVIRONMENT(2),
	DEFAULT_COMPONENT(2),
	DEFAULT_TEAM(1),
	DEFAULT_RATE_LIMIT(100),
	DEFAULT_HISTORY(false),
	DEFAULT_METRICS(false);

	private final Object value;

	PlanDefaults(Object value) {
		this.value = value;
	}

	public Integer getIntValue() {
		return (Integer) value;
	}
}
