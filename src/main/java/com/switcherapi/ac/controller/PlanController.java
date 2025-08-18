package com.switcherapi.ac.controller;

import com.switcherapi.ac.model.domain.Plan;
import com.switcherapi.ac.model.dto.PlanDTO;
import com.switcherapi.ac.model.mapper.DefaultMapper;
import com.switcherapi.ac.model.mapper.PlanMapper;
import com.switcherapi.ac.service.PlanService;
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
	public ResponseEntity<PlanDTO> createPlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		return ResponseEntity.ok(PlanMapper.createCopy(planService.createPlan(plan)));
	}
	
	@Operation(summary = "Update existing plan")
	@PatchMapping(value = "/update")
	public ResponseEntity<PlanDTO> updatePlan(@RequestBody PlanDTO planRequest) {
		final var plan = DefaultMapper.createCopy(planRequest, Plan.class);
		return ResponseEntity.ok(PlanMapper.createCopy(planService.updatePlan(plan.getName(), plan)));
	}
	
	@Operation(summary = "Delete existing plan")
	@DeleteMapping(value = "/delete")
	public ResponseEntity<String> deletePlan(@RequestParam String plan) {
		planService.deletePlan(plan);
		return ResponseEntity.ok("Plan deleted");
	}
	
	@Operation(summary = "List available plans")
	@GetMapping(value = "/list")
	public ResponseEntity<List<PlanDTO>> listPlans() {
		return ResponseEntity.ok(PlanMapper.createCopy(planService.listAll()));
	}
	
	@Operation(summary = "Return one plan")
	@GetMapping(value = "/get")
	public ResponseEntity<PlanDTO> listPlans(@RequestParam String plan) {
		return ResponseEntity.ok(PlanMapper.createCopy(planService.getPlanByName(plan)));
	}
}
