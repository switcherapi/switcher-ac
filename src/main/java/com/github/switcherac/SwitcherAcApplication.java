package com.github.switcherac;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.github.switcherac" })
public class SwitcherAcApplication implements CommandLineRunner {
	
	public static void main(String[] args) {
		SpringApplication.run(SwitcherAcApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
	
	}
	
}
