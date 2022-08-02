package com.github.switcherapi.ac.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.core.HttpHeaders;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminPlanControllerTests {
	
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;
	
	@Autowired
	MockMvc mockMvc;
	
	private String bearer;
	
	private static Admin admin;
	
	private void assertDtoResponse(Plan planObj, String response)
			throws JsonProcessingException {
		var planDto = new ObjectMapper().readValue(response, PlanDTO.class);
		assertThat(planDto.getName()).isEqualTo(planObj.getName());
		assertThat(planDto.getEnableHistory()).isEqualTo(planObj.getEnableHistory());
		assertThat(planDto.getEnableMetrics()).isEqualTo(planObj.getEnableMetrics());
		assertThat(planDto.getMaxComponents()).isEqualTo(planObj.getMaxComponents());
		assertThat(planDto.getMaxDailyExecution()).isEqualTo(planObj.getMaxDailyExecution());
		assertThat(planDto.getMaxDomains()).isEqualTo(planObj.getMaxDomains());
		assertThat(planDto.getMaxEnvironments()).isEqualTo(planObj.getMaxEnvironments());
		assertThat(planDto.getMaxGroups()).isEqualTo(planObj.getMaxGroups());
		assertThat(planDto.getMaxSwitchers()).isEqualTo(planObj.getMaxSwitchers());
		assertThat(planDto.getMaxTeams()).isEqualTo(planObj.getMaxTeams());
	}
	
	@BeforeAll
	static void setup(@Autowired AdminService adminService) {
		admin = adminService.createAdminAccount("123456");
	}
	
	@BeforeEach
	void setup() {
		final var token = jwtService.generateToken(admin.getId())[0];
		
		adminService.updateAdminAccountToken(admin, token);
		bearer = String.format("Bearer %s", token);
	}

	@Test
	void testPlanService() {
		assertThat(planService).isNotNull();
	}
	
	@Test
	void shoutNotDelete_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/admin/v1/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldCreateNewPlan() throws Exception {
		//given
		Plan planObj = Plan.loadDefault();
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		var response = this.mockMvc.perform(post("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.content(json))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())))
			.andReturn().getResponse().getContentAsString();
		
		assertDtoResponse(planObj, response);
	}
	
	@Test
	void shouldUpdatePlan() throws Exception {
		//given
		Plan planObj = Plan.loadDefault();
		planObj.setName(PlanType.DEFAULT.name());
		planObj.setEnableHistory(true);
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		final Plan old = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(false, old.getEnableHistory());
		
		var response = this.mockMvc.perform(patch("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.content(json)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())))
			.andReturn().getResponse().getContentAsString();
		
		assertDtoResponse(planObj, response);
		final Plan planUpdated = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(true, planUpdated.getEnableHistory());
	}
	
	@Test
	void shouldNotUpdatePlan_planNotFound() throws Exception {
		//given
		Plan planObj = Plan.loadDefault();
		planObj.setName("NOT_FOUND");
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(patch("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.content(json))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
	@Test
	void shouldDeletePlan() throws Exception {
		//given
		final Plan planObj = Plan.loadDefault();
		planObj.setName("DELETE_ME");
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		this.mockMvc.perform(post("/admin/v1/plan")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.with(csrf())
				.content(json))
				.andExpect(status().isOk());
		
		//test
		final Plan planBeforeDelete = planService.getPlanByName("DELETE_ME");
		assertThat(planBeforeDelete).isNotNull();
		
		this.mockMvc.perform(delete("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "DELETE_ME"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Plan deleted")));
		
		assertThrows(ResponseStatusException.class, () ->
				planService.getPlanByName("DELETE_ME"), "Unable to find plan DELETE_ME");
	}
	
	@Test
	void shouldNotDeletePlan_planCannotBeDeleted() throws Exception {
		this.mockMvc.perform(delete("/admin/v1/plan")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", PlanType.DEFAULT.name()))
			.andDo(print())
			.andExpect(status().is4xxClientError())
			.andExpect(status().reason(containsString("Invalid plan name")));
	}
	
	@Test
	void shouldListPlans() throws Exception {
		//given
		final List<Plan> plans = planService.listAll();
		assertThat(plans).isNotNull();
		
		//test
		this.mockMvc.perform(get("/admin/v1/plan/list")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())));
	}
	
	@Test
	void shouldGetPlanByName() throws Exception {
		//given
		final Plan plan = planService.getPlanByName(PlanType.DEFAULT.name());
		assertThat(plan).isNotNull();
		
		//test
		var response = this.mockMvc.perform(get("/admin/v1/plan/get")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", PlanType.DEFAULT.name()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())))
			.andReturn().getResponse().getContentAsString();
		
		assertDtoResponse(plan, response);
	}
	
	@Test
	void shouldNotGetPlanByName_plaNotFound() throws Exception {
		this.mockMvc.perform(get("/admin/v1/plan/get")
			.contentType(MediaType.APPLICATION_JSON)
			.header(HttpHeaders.AUTHORIZATION, bearer)
			.with(csrf())
			.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

}
