package com.switcherapi.ac;

import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(basePackages = { "com.switcherapi.ac" })
@Slf4j
public class SwitcherAcApplication implements CommandLineRunner {

	private final PlanService planService;

	public SwitcherAcApplication(PlanService planService) {
		this.planService = planService;
	}

	public static void main(String[] args) {
		SpringApplication.run(SwitcherAcApplication.class, args);
	}

	@Override
	public void run(String... args) {
		log.info("Loading default Plan...");
		planService.createPlan(Plan.loadDefault()).block();
		log.info("Plan loaded");
	}

}
