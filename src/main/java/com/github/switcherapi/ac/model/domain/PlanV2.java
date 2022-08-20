package com.github.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Generated
@JsonInclude(Include.NON_NULL)
@Document(collection = "plansV2")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanV2 {

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private List<PlanAttribute> attributes = new ArrayList<>();

	public PlanAttribute getFeature(String feature) {
		return attributes.stream().filter(a -> a.getFeature().equals(feature))
				.findFirst()
				.orElseThrow();
	}

	public boolean hasFeature(String feature) {
		return attributes.stream().anyMatch(a -> a.getFeature().equals(feature));
	}

	public void addFeature(String feature, Object value) {
		attributes.add(PlanAttribute.builder()
				.feature(feature)
				.value(value).build());
	}

	public static PlanV2 loadDefault() {
		return PlanV2.builder()
			.name(PlanType.DEFAULT.name())
			.attributes(Arrays.asList(
				PlanAttribute.builder().feature("domain").value(1).build(),
				PlanAttribute.builder().feature("group").value(2).build(),
				PlanAttribute.builder().feature("switcher").value(3).build(),
				PlanAttribute.builder().feature("environment").value(2).build(),
				PlanAttribute.builder().feature("component").value(2).build(),
				PlanAttribute.builder().feature("team").value(1).build(),
				PlanAttribute.builder().feature("daily_execution").value(100).build(),
				PlanAttribute.builder().feature("history").value(false).build(),
				PlanAttribute.builder().feature("metrics").value(false).build()
			)).build();
	}

}
