package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.github.switcherapi.ac.model.Admin;
import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class AdminPlanControllerTests {
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private JwtTokenService jwtService;
	
	@Autowired
	private PlanService planService;
	
	@Autowired
	private MockMvc mockMvc;
	
	private String token;
	
	@BeforeEach
	void setup() {
		final Admin admin = adminService.createAdminAccount("123456");
		token = jwtService.generateToken(admin.getId())[0];
		adminService.updateAdminAccountToken(admin, token);
	}

	@Test
	void testPlanService() {
		assertThat(planService).isNotNull();
	}
	
	@Test
	void shoutNotDelete_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf())
			.queryParam("plan", "BASIC"))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldCreateNewPlan() throws Exception {
		//given
		PlanDTO planObj = Plan.loadDefault();
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(post("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.content(json))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())));
	}
	
	@Test
	void shouldUpdatePlan() throws Exception {
		//given
		PlanDTO planObj = Plan.loadDefault();
		planObj.setName(PlanType.DEFAULT.name());
		planObj.setEnableHistory(true);
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		final Plan old = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(false, old.getEnableHistory());
		
		this.mockMvc.perform(patch("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.content(json)
			.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())));
		
		final Plan planUpdated = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(true, planUpdated.getEnableHistory());
	}
	
	@Test
	void shouldNotUpdatePlan_planNotFound() throws Exception {
		//given
		PlanDTO planObj = Plan.loadDefault();
		planObj.setName("NOT_FOUND");
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(patch("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.content(json))
			.andDo(print())
			.andExpect(status().isNotFound());
	}
	
	@Test
	void shouldDeletePlan() throws Exception {
		//given
		final PlanDTO planObj = Plan.loadDefault();
		planObj.setName("DELETE_ME");
		
		Gson gson = new Gson();
		String json = gson.toJson(planObj);
		
		this.mockMvc.perform(post("/admin/plan/v1")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + token)
				.with(csrf())
				.content(json))
				.andExpect(status().isOk());
		
		//test
		final Plan planBeforeDelete = planService.getPlanByName("DELETE_ME");
		assertThat(planBeforeDelete).isNotNull();
		
		this.mockMvc.perform(delete("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", "DELETE_ME"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("Plan deleted")));
		
		assertThrows(ResponseStatusException.class, () -> {
			planService.getPlanByName("DELETE_ME");
	    }, "Unable to find plan DELETE_ME");
	}
	
	@Test
	void shouldNotDeletePlan_planCannotBeDeleted() throws Exception {
		this.mockMvc.perform(delete("/admin/plan/v1")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
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
		this.mockMvc.perform(get("/admin/plan/v1/list")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
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
		this.mockMvc.perform(get("/admin/plan/v1/get")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", PlanType.DEFAULT.name()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())));
	}
	
	@Test
	void shouldNotGetPlanByName_plaNotFound() throws Exception {
		this.mockMvc.perform(get("/admin/plan/v1/get")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + token)
			.with(csrf())
			.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

}
