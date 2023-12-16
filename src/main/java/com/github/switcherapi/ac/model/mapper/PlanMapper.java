package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanMapper {

	public static <T> List<PlanDTO> createCopy(List<T> from) {
		return from.stream()
				.map(item -> DefaultMapper.createCopy(item, PlanDTO.class))
				.toList();
	}

	public static void copyProperties(Plan from, Plan to) {
		DefaultMapper.copyProperties(from, to, "attributes");
		from.getAttributes().forEach(planAttribute -> {
			if (to.hasFeature(planAttribute.getFeature()))
				to.getFeature(planAttribute.getFeature()).setValue(planAttribute.getValue());
			else
				to.addFeature(planAttribute.getFeature(), planAttribute.getValue());
		});
	}
}
