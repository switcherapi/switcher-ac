package com.github.switcherapi.ac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static com.github.switcherapi.ac.config.SwitcherFeatures.*;
import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.service.PlanService;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.switcherapi.ac" })
public class SwitcherAcApplication implements CommandLineRunner {
	
	private static final Logger logger = LogManager.getLogger(SwitcherAcApplication.class);
	
	@Autowired
	private PlanService planService;
	
	public static void main(String[] args) {
		SpringApplication.run(SwitcherAcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		checkSwitchers();
		
		logger.info("Loading default Plan...");
		planService.createPlan(Plan.loadDefault());
		logger.info("Plan loaded");
	}
	
}
