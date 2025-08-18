package com.switcherapi.ac.util;

import lombok.Getter;

@Getter
public enum Constants {

    MSG_INVALID_FEATURE("Invalid feature"),
    MSG_FEATURE_LIMIT_REACHED("Feature limit has been reached"),
    MSG_FEATURE_MISSING("Feature is missing"),
    MSG_OWNER_MISSING("Owner is missing"),
    MSG_PLAN_INVALID_VALUE("Plan has invalid value"),

    ACCOUNT_NOT_FOUND ("Account not found"),
    PLAN_NAME_NOT_FOUND("Unable to find plan named %s"),
    ACCOUNT_NAME_NOT_FOUND("Unable to find account %s"),

    BEARER("Bearer ");

    private final String value;

    Constants(String value) {
        this.value = value;
    }

}
