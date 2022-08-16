package com.github.switcherapi.ac;

import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.service.PlanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static com.github.switcherapi.ac.config.SwitcherFeatures.checkSwitchers;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.switcherapi.ac" })
public class SwitcherAcApplication implements CommandLineRunner {

	private static final Logger logger = LogManager.getLogger(SwitcherAcApplication.class);

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

		logger.info("Loading default Plan...");
		planService.createPlan(Plan.loadDefault());
		planService.createPlanV2(PlanV2.loadDefault());
		logger.info("Plan loaded");
	}

}
