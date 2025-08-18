package com.switcherapi.ac.model.mapper;

import com.switcherapi.ac.model.domain.Feature;
import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.domain.PlanAttribute;
import com.switcherapi.ac.model.dto.PlanDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanMapper {

	public static <T> List<PlanDTO> createCopy(List<T> from) {
		return from.stream()
				.map(item -> {
					if (item instanceof Plan plan) {
						return new PlanDTO(plan.getId(), plan.getName(), plan.getAttributes());
					}

					return null;
				}).toList();
	}

	public static PlanDTO createCopy(Plan from) {
		final var attributes = new ArrayList<PlanAttribute>();
		from.getAttributes().forEach(planAttribute ->
				attributes.add(DefaultMapper.createCopy(planAttribute, PlanAttribute.class)));

		return new PlanDTO(from.getId(), from.getName(), attributes);
	}

	public static void copyProperties(Plan from, Plan to) {
		DefaultMapper.copyProperties(from, to, "attributes");
		from.getAttributes().forEach(planAttribute -> {
			var feature = Feature.getFeatureEnum(planAttribute.getFeature());

			if (to.hasFeature(feature)) {
				to.getFeature(feature).setValue(planAttribute.getValue());
			} else {
				to.addFeature(feature, planAttribute.getValue());
			}
		});
	}

	public static void copyProperties(PlanDTO from, Plan to) {
		DefaultMapper.copyProperties(from, to, "attributes");
		from.attributes().forEach(planAttribute -> {
			var feature = Feature.getFeatureEnum(planAttribute.getFeature());

			if (to.hasFeature(feature)) {
				to.getFeature(feature).setValue(planAttribute.getValue());
			} else {
				to.addFeature(feature, planAttribute.getValue());
			}
		});
	}
}
