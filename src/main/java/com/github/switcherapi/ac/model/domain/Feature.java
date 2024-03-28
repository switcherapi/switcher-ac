package com.github.switcherapi.ac.model.domain;

import lombok.Getter;

import static com.github.switcherapi.ac.util.Constants.MSG_INVALID_FEATURE;

@Getter
public enum Feature {
    DOMAIN("domain"),
    GROUP("group"),
    SWITCHER("switcher"),
    ENVIRONMENT("environment"),
    COMPONENT("component"),
    TEAM("team"),
    RATE_LIMIT("rate_limit"),
    HISTORY("history"),
    METRICS("metrics");

    private final String value;

    Feature(String value) {
        this.value = value;
    }

    public static Feature getFeatureEnum(String value) {
        for (Feature feature : Feature.values()) {
            if (feature.getValue().equals(value)) {
                return feature;
            }
        }

        throw new IllegalArgumentException(String.format("%s: %s", MSG_INVALID_FEATURE.getValue(), value));
    }

}
