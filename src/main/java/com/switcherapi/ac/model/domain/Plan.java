package com.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.switcherapi.ac.model.domain.Feature.*;
import static com.switcherapi.ac.model.domain.PlanDefaults.*;

@Generated
@JsonInclude(Include.NON_NULL)
@Document(collection = "plans")
@Data
@Builder
@AllArgsConstructor
public class Plan {

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private List<PlanAttribute> attributes;

	public Plan() {
		attributes = new ArrayList<>();
	}

	public PlanAttribute getFeature(Feature feature) {
		return attributes.stream().filter(a -> a.getFeature().equals(feature.getValue()))
				.findFirst()
				.orElseThrow();
	}

	public boolean hasFeature(Feature feature) {
		return attributes.stream().anyMatch(a -> a.getFeature().equals(feature.getValue()));
	}

	public void addFeature(Feature feature, Object value) {
		attributes.add(PlanAttribute.builder()
				.feature(feature.getValue())
				.value(value).build());
	}

	public static Plan loadDefault() {
		return Plan.builder()
			.name(PlanType.DEFAULT.name())
			.attributes(Arrays.asList(
				PlanAttribute.builder().feature(DOMAIN.getValue()).value(DEFAULT_DOMAIN.getIntValue()).build(),
				PlanAttribute.builder().feature(GROUP.getValue()).value(DEFAULT_GROUP.getIntValue()).build(),
				PlanAttribute.builder().feature(SWITCHER.getValue()).value(DEFAULT_SWITCHER.getIntValue()).build(),
				PlanAttribute.builder().feature(ENVIRONMENT.getValue()).value(DEFAULT_ENVIRONMENT.getIntValue()).build(),
				PlanAttribute.builder().feature(COMPONENT.getValue()).value(DEFAULT_COMPONENT.getIntValue()).build(),
				PlanAttribute.builder().feature(TEAM.getValue()).value(DEFAULT_TEAM.getIntValue()).build(),
				PlanAttribute.builder().feature(RATE_LIMIT.getValue()).value(DEFAULT_RATE_LIMIT.getIntValue()).build(),
				PlanAttribute.builder().feature(HISTORY.getValue()).value(DEFAULT_HISTORY.getValue()).build(),
				PlanAttribute.builder().feature(METRICS.getValue()).value(DEFAULT_METRICS.getValue()).build()
			)).build();
	}

}
