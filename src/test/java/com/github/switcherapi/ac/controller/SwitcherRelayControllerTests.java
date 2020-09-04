package com.github.switcherapi.ac.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.github.switcherapi.ac.model.Plan;
import com.github.switcherapi.ac.model.PlanDTO;
import com.github.switcherapi.ac.model.PlanType;
import com.github.switcherapi.ac.model.request.RequestRelay;
import com.github.switcherapi.ac.model.response.ResponseRelay;
import com.github.switcherapi.ac.service.AccountService;
import com.github.switcherapi.ac.service.PlanService;
import com.google.gson.Gson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
class SwitcherRelayControllerTests {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private PlanService planService;
	
	@Autowired
	private MockMvc mockMvc;
	
	private final Gson GSON = new Gson();
	
	private void executeTestValidate(String adminId, String featureName, 
			String value, ResponseRelay expectedResponse, int expectedStatus) throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setValue(String.format("%s#%s", featureName, adminId));
		
		if (value != null)
			request.setNumeric(value);
		
		String jsonRequest = GSON.toJson(request);
		String jsonResponse = GSON.toJson(expectedResponse);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().is(expectedStatus))
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	private void executeTestExecution(String value, ResponseRelay expectedResponse, 
			int expectedStatus) throws Exception {
		//given
		String jsonResponse = GSON.toJson(expectedResponse);
		
		//test
		this.mockMvc.perform(get("/switcher/v1/execution")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.queryParam("value", value))
			.andDo(print())
			.andExpect(status().is(expectedStatus))
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shoutNotCreate_notAuthenticated() throws Exception {
		this.mockMvc.perform(delete("/switcher/v1/create")
			.contentType(MediaType.APPLICATION_JSON)
			.content(""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void testServices() {
		assertThat(accountService).isNotNull();
	}
	
	@Test
	void shouldCreateAccount() throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setValue("adminid");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/create")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldRemoveAccount() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("adminid");
		
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/remove")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotRemoveAccount_accountNotFound() throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setValue("NOT_FOUND");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "404 NOT_FOUND \"Unable to find account NOT_FOUND\"");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/remove")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().is5xxServerError())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_execution() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);
		
		//test
		this.executeTestExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution() throws Exception {
		//given
		accountService.createAccount("adminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setMaxDailyExecution(0);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelay expectedResponse = new ResponseRelay(false, "Daily execution limit has been reached");

		//test
		this.executeTestExecution("adminid", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution_accountNotFound() throws Exception {
		//given
		ResponseRelay expectedResponse = new ResponseRelay(false, "404 NOT_FOUND \"Account not found\"");
		
		//test
		this.executeTestExecution("NOT_FOUND", expectedResponse, 500);
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() throws Exception {
		//given
		accountService.createAccount("masteradminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setMaxSwitchers(-1);
		plan.setName("ILIMITED");
		planService.createPlan(plan);
		accountService.updateAccountPlan("masteradminid", "ILIMITED");
		
		ResponseRelay expectedResponse = new ResponseRelay(true);
		
		//test
		this.executeTestValidate("masteradminid", "switcher", "10000", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_domain() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "domain", "0", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_domain() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Domain limit has been reached");
		
		//test
		this.executeTestValidate("adminid", "domain", "2", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_group() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "group", "1", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_group() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Group limit has been reached");

		//test
		this.executeTestValidate("adminid", "group", "5", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_switcher() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "switcher", "1", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_switcher() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Switcher limit has been reached");
		
		//test
		this.executeTestValidate("adminid", "switcher", "4", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_component() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "component", "1", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_component() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Component limit has been reached");

		//test
		this.executeTestValidate("adminid", "component", "3", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_environment() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);
		
		//test
		this.executeTestValidate("adminid", "environment", "1", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_environment() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Environment limit has been reached");
		
		//test
		this.executeTestValidate("adminid", "environment", "3", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_team() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(true);
		
		//test
		this.executeTestValidate("adminid", "team", "0", expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_team() throws Exception {
		//given
		accountService.createAccount("adminid");
		ResponseRelay expectedResponse = new ResponseRelay(false, "Team limit has been reached");

		//test
		this.executeTestValidate("adminid", "team", "2", expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_metric() throws Exception {
		//given
		accountService.createAccount("adminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setEnableMetrics(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "metrics", null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_metric() throws Exception {
		//given
		accountService.createAccount("adminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setEnableMetrics(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelay expectedResponse = new ResponseRelay(false, "Metrics is not available");
		
		//test
		this.executeTestValidate("adminid", "metrics", null, expectedResponse, 200);
	}
	
	@Test
	void shouldBeOkWhenValidate_history() throws Exception {
		//given
		accountService.createAccount("adminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setEnableHistory(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelay expectedResponse = new ResponseRelay(true);

		//test
		this.executeTestValidate("adminid", "history", null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_history() throws Exception {
		//given
		accountService.createAccount("adminid");
		PlanDTO plan = Plan.loadDefault();
		plan.setEnableHistory(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		ResponseRelay expectedResponse = new ResponseRelay(false, "History is not available");
		
		//test
		this.executeTestValidate("adminid", "history", null, expectedResponse, 200);
	}
	
	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() throws Exception {
		//given
		ResponseRelay expectedResponse = new ResponseRelay(false, "404 NOT_FOUND \"Account not found\"");
		final String[] features = new String[] {
				"domain", "group", "switcher", "component",
				"environment", "team", "metrics", "history"
		};
		
		//test
		for (String feature : features) {
			this.executeTestValidate("NOT_FOUND", feature, "0", expectedResponse, 500);
		}
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() throws Exception {
		//given
		ResponseRelay expectedResponse = new ResponseRelay(false, 
				"Invalid arguments - value INVALID_FEATURE#adminid - numeric 0");
		
		//test
		this.executeTestValidate("adminid", "INVALID_FEATURE", "0", expectedResponse, 500);
	}

}
