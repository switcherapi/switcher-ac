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
				PlanAttribute.builder().feature(DOMAIN.getValue()).value(1).build(),
				PlanAttribute.builder().feature(GROUP.getValue()).value(2).build(),
				PlanAttribute.builder().feature(SWITCHER.getValue()).value(3).build(),
				PlanAttribute.builder().feature(ENVIRONMENT.getValue()).value(2).build(),
				PlanAttribute.builder().feature(COMPONENT.getValue()).value(2).build(),
				PlanAttribute.builder().feature(TEAM.getValue()).value(1).build(),
				PlanAttribute.builder().feature(RATE_LIMIT.getValue()).value(100).build(),
				PlanAttribute.builder().feature(HISTORY.getValue()).value(false).build(),
				PlanAttribute.builder().feature(METRICS.getValue()).value(false).build()
			)).build();
	}

}
