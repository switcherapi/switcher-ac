package com.github.switcherapi.ac.model.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.github.switcherapi.ac.model.dto.PlanDTO;

import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanMapper {
	
	public static <T> List<PlanDTO> createCopy(List<T> from) {
		return from.stream()
			.map(item -> DefaultMapper.createCopy(item, new PlanDTO()))
			.collect(Collectors.toList());
	}

}
