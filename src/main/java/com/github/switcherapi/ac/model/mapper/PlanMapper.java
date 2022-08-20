package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.model.dto.PlanV2DTO;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanMapper {

	public static <T> List<PlanV2DTO> createCopyV2(List<T> from) {
		return from.stream()
				.map(item -> DefaultMapper.createCopy(item, new PlanV2DTO()))
				.collect(Collectors.toList());
	}

	public static void copyProperties(PlanV2 from, PlanV2 to) {
		DefaultMapper.copyProperties(from, to, "attributes");
		from.getAttributes().forEach(planAttribute -> {
			if (to.hasFeature(planAttribute.getFeature()))
				to.getFeature(planAttribute.getFeature()).setValue(planAttribute.getValue());
			else
				to.addFeature(planAttribute.getFeature(), planAttribute.getValue());
		});
	}
}
