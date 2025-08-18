package com.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;

@JsonInclude(Include.NON_NULL)
@Builder
public record FeaturePayload(
        String owner,
        String feature,
        Integer total) {
}
