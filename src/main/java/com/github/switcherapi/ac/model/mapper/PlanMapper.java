package com.github.switcherapi.ac.model.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.github.switcherapi.ac.model.dto.PlanDTO;

public class PlanMapper {
	
	private PlanMapper() {}
	
	public static <T> List<PlanDTO> createCopy(List<T> from) {
		return from.stream()
			.map(item -> DefaultMapper.createCopy(item, PlanDTO.class))
			.collect(Collectors.toList());
	}

}
