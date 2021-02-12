package com.github.switcherapi.ac.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.service.util.PlanUtils;

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
		PlanUtils.loadAttributes(from, to);
		assertThat(to.getMaxDomains()).isEqualTo(2);
		assertThat(to.getEnableHistory()).isEqualTo(false);
	}
	
	@Test
	void shouldPopulatePlan() {
		//given
		final PlanDTO from = Plan.loadDefault();
		Plan to = new Plan();
		
		//test
		PlanUtils.loadAttributes(from, to);
		assertThat(from.getName()).isEqualTo(to.getName());
		assertThat(from.getMaxDomains()).isEqualTo(to.getMaxDomains());
		assertThat(from.getEnableHistory()).isEqualTo(to.getEnableHistory());
	}

}
