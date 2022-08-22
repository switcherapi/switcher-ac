package com.github.switcherapi.ac;

import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PlanUtilsTests {
	
	@Test
	void shouldConvertDTO() {
		//given
		final var to = Plan.builder()
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
		
		final PlanDTO from = new PlanDTO();
		from.addFeature("domain", 2);
		
		//test
		PlanMapper.copyProperties(from, to);
		assertThat(to.getFeature("domain").getValue()).isEqualTo(2);
		assertThat(Boolean.parseBoolean(to.getFeature("history").getValue().toString())).isFalse();
	}
	
	@Test
	void shouldPopulatePlan() {
		//given
		final var from = Plan.loadDefault();
		Plan to = new Plan();
		
		//test
		PlanMapper.copyProperties(from, to);
		assertThat(from.getName()).isEqualTo(to.getName());
		assertThat(from.getFeature("domain").getValue()).isEqualTo(to.getFeature("domain").getValue());
		assertThat(from.getFeature("history").getValue()).isEqualTo(to.getFeature("history").getValue());
	}

}
