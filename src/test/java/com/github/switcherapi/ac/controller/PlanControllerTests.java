package com.github.switcherapi.ac.controller;

import com.github.switcherapi.ac.controller.fixture.ControllerTestUtils;
import com.github.switcherapi.ac.model.domain.Admin;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.service.AdminService;
import com.github.switcherapi.ac.service.JwtTokenService;
import com.github.switcherapi.ac.service.PlanService;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import static com.github.switcherapi.ac.model.domain.Feature.HISTORY;
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
class PlanControllerTests extends ControllerTestUtils {
	
	@Autowired AdminService adminService;
	@Autowired JwtTokenService jwtService;
	@Autowired PlanService planService;

	private String bearer;
	private static Admin admin;
	
	@BeforeAll
	static void setup(@Autowired AdminService adminService) {
		admin = adminService.createAdminAccount("123456");
	}
	
	@BeforeEach
	void setup() {
		final var token = jwtService.generateToken(admin.getId()).getLeft();
		
		adminService.updateAdminAccountToken(admin, token);
		bearer = String.format("Bearer %s", token);
	}

	@Test
	void testPlanService() {
		assertThat(planService).isNotNull();
	}
	
	@Test
	void shoutNotDelete_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/plan/v2/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.with(csrf())
				.queryParam("plan", "BASIC"))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void shouldCreateNewPlan() throws Exception {
		//given
		var planObj = Plan.loadDefault();
		var json = gson.toJson(planObj);
		
		//test
		var response = this.mockMvc.perform(post("/plan/v2/create")
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
		var planObj = Plan.loadDefault();
		planObj.setName(PlanType.DEFAULT.name());
		planObj.getFeature(HISTORY).setValue(true);
		var json = gson.toJson(planObj);
		
		//test
		final var old = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(false, old.getFeature(HISTORY).getValue());
		
		var response = this.mockMvc.perform(patch("/plan/v2/update")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.content(json)
				.with(csrf()))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(PlanType.DEFAULT.name())))
			.andReturn().getResponse().getContentAsString();
		
		assertDtoResponse(planObj, response);
		final var planUpdated = planService.getPlanByName(PlanType.DEFAULT.name());
		assertEquals(true, planUpdated.getFeature(HISTORY).getValue());
	}
	
	@Test
	void shouldNotUpdatePlan_planNotFound() throws Exception {
		//given
		var planObj = Plan.loadDefault();
		planObj.setName("NOT_FOUND");
		var json = gson.toJson(planObj);
		
		//test
		this.mockMvc.perform(patch("/plan/v2/update")
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
		final var planObj = Plan.loadDefault();
		planObj.setName("DELETE_ME");
		var json = gson.toJson(planObj);
		
		this.mockMvc.perform(post("/plan/v2/create")
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, bearer)
					.with(csrf())
					.content(json))
				.andExpect(status().isOk());
		
		//test
		final var planBeforeDelete = planService.getPlanByName("DELETE_ME");
		assertThat(planBeforeDelete).isNotNull();
		
		this.mockMvc.perform(delete("/plan/v2/delete")
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
		this.mockMvc.perform(delete("/plan/v2/delete")
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
		final var plans = planService.listAll();
		assertThat(plans).isNotNull();
		
		//test
		this.mockMvc.perform(get("/plan/v2/list")
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
		final var plan = planService.getPlanByName(PlanType.DEFAULT.name());
		assertThat(plan).isNotNull();
		
		//test
		var response = this.mockMvc.perform(get("/plan/v2/get")
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
		this.mockMvc.perform(get("/plan/v2/get")
				.contentType(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, bearer)
				.with(csrf())
				.queryParam("plan", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().isNotFound());
	}

}
