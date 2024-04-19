package com.github.switcherapi.ac.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;

@Builder
public record Metadata(
        @JsonProperty("rate_limit")
        @SerializedName("rate_limit")
        int rateLimit
) {
}
