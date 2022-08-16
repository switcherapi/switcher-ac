package com.github.switcherapi.ac.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Data;
import lombok.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;
import java.util.List;

@Generated
@JsonInclude(Include.NON_NULL)
@Document(collection = "plansV2")
@Data
@Builder
public class PlanV2 {

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private List<PlanAttribute> attributes;

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
					PlanAttribute.builder().feature("enable_history").value(false).build(),
					PlanAttribute.builder().feature("enable_metric").value(false).build()
			)).build();
	}

}
