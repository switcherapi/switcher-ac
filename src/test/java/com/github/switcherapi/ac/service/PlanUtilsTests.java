package com.github.switcherapi.ac.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.mapper.DefaultMapper;

class PlanUtilsTests {
	
	@Test
	void shouldConvertDTO() {
		//given
		final Plan to = new Plan();
		to.setName(PlanType.DEFAULT.name());
		to.setMaxDomains(1);
		to.setMaxGroups(2);
		to.setMaxSwitchers(3);
		to.setMaxEnvironments(2);
		to.setMaxComponents(2);
		to.setMaxTeams(1);
		to.setMaxDailyExecution(100);
		to.setEnableHistory(false);
		to.setEnableMetrics(false);
		
		final PlanDTO from = new PlanDTO();
		from.setMaxDomains(2);
		
		//test
		DefaultMapper.copyProperties(from, to);
		assertThat(to.getMaxDomains()).isEqualTo(2);
		assertThat(to.getEnableHistory()).isFalse();
	}
	
	@Test
	void shouldPopulatePlan() {
		//given
		final Plan from = Plan.loadDefault();
		Plan to = new Plan();
		
		//test
		DefaultMapper.copyProperties(from, to);
		assertThat(from.getName()).isEqualTo(to.getName());
		assertThat(from.getMaxDomains()).isEqualTo(to.getMaxDomains());
		assertThat(from.getEnableHistory()).isEqualTo(to.getEnableHistory());
	}

}
