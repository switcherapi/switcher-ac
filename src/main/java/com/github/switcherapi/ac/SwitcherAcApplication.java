package com.github.switcherapi.ac;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static com.github.switcherapi.ac.config.SwitcherFeatures.checkSwitchers;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.switcherapi.ac" })
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
		checkSwitchers();

		log.info("Loading default Plan...");
		planService.createPlan(Plan.loadDefault());
		log.info("Plan loaded");
	}

}
