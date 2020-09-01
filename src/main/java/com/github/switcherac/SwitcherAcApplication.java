package com.github.switcherac;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.switcherac.model.Plan;
import com.github.switcherac.model.PlanType;
import com.github.switcherac.service.PlanService;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.switcherac" })
public class SwitcherAcApplication implements CommandLineRunner {
	
	private static final Logger logger = LogManager.getLogger(SwitcherAcApplication.class);
	
	@Autowired
	private PlanService planService;
	
	public static void main(String[] args) {
		SpringApplication.run(SwitcherAcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info(String.format("Loading %s Plan...", PlanType.DEFAULT.name()));
		planService.createPlan(Plan.loadDefault());
		logger.info("DEFAULT Plan loaded");
	}
	
}
