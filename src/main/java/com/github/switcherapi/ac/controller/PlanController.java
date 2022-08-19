package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.model.domain.PlanV2;
import com.github.switcherapi.ac.model.dto.PlanV2DTO;
import com.github.switcherapi.ac.model.mapper.DefaultMapper;
import com.github.switcherapi.ac.model.mapper.PlanMapper;
import com.github.switcherapi.ac.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	public ResponseEntity<PlanV2DTO> createPlan(@RequestBody PlanV2DTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, new PlanV2());
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.createPlanV2(plan), new PlanV2DTO()));
	}
	
	@Operation(summary = "Update existing plan")
	@PatchMapping(value = "/update")
	public ResponseEntity<PlanV2DTO> updatePlan(@RequestBody PlanV2DTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, new PlanV2());
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.updatePlanV2(plan.getName(), plan), new PlanV2DTO()));
	}
	
	@Operation(summary = "Delete existing plan")
	@DeleteMapping(value = "/delete")
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		planService.deletePlanV2(plan);
		return ResponseEntity.ok("Plan deleted");
	}
	
	@Operation(summary = "List available plans")
	@GetMapping(value = "/list")
	public ResponseEntity<List<PlanV2DTO>> listPlans() {
		return ResponseEntity.ok(PlanMapper.createCopyV2(planService.listAllV2()));
	}
	
	@Operation(summary = "Return one plan")
	@GetMapping(value = "/get")
	public ResponseEntity<PlanV2DTO> listPlans(@RequestParam String plan) {
		return ResponseEntity.ok(DefaultMapper.createCopy(planService.getPlanV2ByName(plan), new PlanV2DTO()));
	}
}
