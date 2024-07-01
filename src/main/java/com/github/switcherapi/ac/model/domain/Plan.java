package com.github.switcherapi.ac.model.domain;

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

import static com.github.switcherapi.ac.model.domain.Feature.*;

@Generated
@JsonInclude(Include.NON_NULL)
@Document(collection = "plans")
@Data
@Builder
@AllArgsConstructor
public class Plan {

	public static final int DEFAULT_DOMAIN = 1;
	public static final int DEFAULT_GROUP = 2;
	public static final int DEFAULT_SWITCHER = 3;
	public static final int DEFAULT_ENVIRONMENT = 2;
	public static final int DEFAULT_COMPONENT = 2;
	public static final int DEFAULT_TEAM = 1;
	public static final int DEFAULT_RATE_LIMIT = 100;
	public static final boolean DEFAULT_HISTORY = false;
	public static final boolean DEFAULT_METRICS = false;

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
				PlanAttribute.builder().feature(DOMAIN.getValue()).value(DEFAULT_DOMAIN).build(),
				PlanAttribute.builder().feature(GROUP.getValue()).value(DEFAULT_GROUP).build(),
				PlanAttribute.builder().feature(SWITCHER.getValue()).value(DEFAULT_SWITCHER).build(),
				PlanAttribute.builder().feature(ENVIRONMENT.getValue()).value(DEFAULT_ENVIRONMENT).build(),
				PlanAttribute.builder().feature(COMPONENT.getValue()).value(DEFAULT_COMPONENT).build(),
				PlanAttribute.builder().feature(TEAM.getValue()).value(DEFAULT_TEAM).build(),
				PlanAttribute.builder().feature(RATE_LIMIT.getValue()).value(DEFAULT_RATE_LIMIT).build(),
				PlanAttribute.builder().feature(HISTORY.getValue()).value(DEFAULT_HISTORY).build(),
				PlanAttribute.builder().feature(METRICS.getValue()).value(DEFAULT_METRICS).build()
			)).build();
	}

}
