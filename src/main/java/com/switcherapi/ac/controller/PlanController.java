package com.switcherapi.ac.controller;

import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.dto.PlanDTO;
import com.switcherapi.ac.model.mapper.DefaultMapper;
import com.switcherapi.ac.model.mapper.PlanMapper;
import com.switcherapi.ac.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("plan/v2")
public class PlanController {

	private final PlanService planService;

	public PlanController(PlanService planService) {
		this.planService = planService;
	}
	
	@Operation(summary = "Create a new plan")
	@PostMapping(value = "/create")
	public Mono<ResponseEntity<PlanDTO>> createPlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		return planService.createPlan(plan)
				.map(PlanMapper::createCopy)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.badRequest().build());
	}
	
	@Operation(summary = "Update existing plan")
	@PatchMapping(value = "/update")
	public Mono<ResponseEntity<PlanDTO>> updatePlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		return planService.updatePlan(plan.getName(), plan)
				.map(PlanMapper::createCopy)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.badRequest().build());
	}
	
	@Operation(summary = "Delete existing plan")
	@DeleteMapping(value = "/delete")
	public Mono<ResponseEntity<String>> deletePlan(@RequestParam String plan) {
		return planService.deletePlan(plan)
				.map(deleted -> ResponseEntity.ok("Plan deleted"));
	}
	
	@Operation(summary = "List available plans")
	@GetMapping(value = "/list")
	public Mono<ResponseEntity<List<PlanDTO>>> listPlans() {
		return planService.listAll()
				.collectList()
				.map(plans -> ResponseEntity.ok(PlanMapper.createCopy(plans)))
				.defaultIfEmpty(ResponseEntity.ok(List.of()));
	}
	
	@Operation(summary = "Return one plan")
	@GetMapping(value = "/get")
	public Mono<ResponseEntity<PlanDTO>> listPlans(@RequestParam String plan) {
		return planService.getPlanByName(plan)
				.map(PlanMapper::createCopy)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.badRequest().build());
	}
}
