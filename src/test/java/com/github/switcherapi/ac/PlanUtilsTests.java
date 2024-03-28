package com.github.switcherapi.ac;

import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.github.switcherapi.ac.model.domain.Feature.*;
import static org.assertj.core.api.Assertions.assertThat;

class PlanUtilsTests {
	
	@Test
	void shouldConvertDTO() {
		//given
		final var to = Plan.builder()
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
		
		final PlanDTO from = new PlanDTO();
		from.addFeature(DOMAIN, 2);
		
		//test
		PlanMapper.copyProperties(from, to);
		assertThat(to.getFeature(DOMAIN).getValue()).isEqualTo(2);
		assertThat(Boolean.parseBoolean(to.getFeature(HISTORY).getValue().toString())).isFalse();
	}
	
	@Test
	void shouldPopulatePlan() {
		//given
		final var from = Plan.loadDefault();
		Plan to = new Plan();
		
		//test
		PlanMapper.copyProperties(from, to);
		assertThat(from.getName()).isEqualTo(to.getName());
		assertThat(from.getFeature(DOMAIN).getValue()).isEqualTo(to.getFeature(DOMAIN).getValue());
		assertThat(from.getFeature(HISTORY).getValue()).isEqualTo(to.getFeature(HISTORY).getValue());
	}

}
