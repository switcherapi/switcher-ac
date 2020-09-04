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
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(get("/switcher/v1/execution")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.queryParam("value", "adminid"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution() throws Exception {
		//given
		Plan plan = accountService.createAccount("adminid").getPlan();
		plan.setMaxDailyExecution(0);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		
		ResponseRelay response = new ResponseRelay(false, "Daily execution limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(get("/switcher/v1/execution")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.queryParam("value", "adminid"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_execution_accountNotFound() throws Exception {
		//given
		ResponseRelay response = new ResponseRelay(false, "404 NOT_FOUND \"Account not found\"");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(get("/switcher/v1/execution")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.queryParam("value", "NOT_FOUND"))
			.andDo(print())
			.andExpect(status().is5xxServerError())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_unlimitedUseFeature() throws Exception {
		//given
		Plan plan = accountService.createAccount("masteradminid").getPlan();
		plan.setId(null);
		plan.setMaxSwitchers(-1);
		plan.setName("ILIMITED");
		planService.createPlan(plan);
		accountService.updateAccountPlan("masteradminid", "ILIMITED");
		
		RequestRelay request = new RequestRelay();
		request.setValue("switcher#masteradminid");
		request.setNumeric("10000");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_domain() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("domain#adminid");
		request.setNumeric("0");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_domain() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("domain#adminid");
		request.setNumeric("2"); // limit of 1
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Domain limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_group() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("group#adminid");
		request.setNumeric("1");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_group() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("group#adminid");
		request.setNumeric("5"); // limit of 2
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Group limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_switcher() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("switcher#adminid");
		request.setNumeric("1");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_switcher() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("switcher#adminid");
		request.setNumeric("4"); // limit of 3
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Switcher limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_component() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("component#adminid");
		request.setNumeric("1");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_component() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("component#adminid");
		request.setNumeric("3"); // limit of 2
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Component limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_environment() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("environment#adminid");
		request.setNumeric("1");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_environment() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("environment#adminid");
		request.setNumeric("3"); // limit of 2
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Environment limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_team() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("team#adminid");
		request.setNumeric("0");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_team() throws Exception {
		//given
		accountService.createAccount("adminid");
		
		RequestRelay request = new RequestRelay();
		request.setValue("team#adminid");
		request.setNumeric("2"); // limit of 1
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Team limit has been reached");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_metric() throws Exception {
		//given
		Plan plan = accountService.createAccount("adminid").getPlan();
		plan.setEnableMetrics(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		
		RequestRelay request = new RequestRelay();
		request.setValue("metrics#adminid");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_metric() throws Exception {
		//given
		Plan plan = accountService.createAccount("adminid").getPlan();
		plan.setEnableMetrics(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		
		RequestRelay request = new RequestRelay();
		request.setValue("metrics#adminid");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Metrics is not available");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldBeOkWhenValidate_history() throws Exception {
		//given
		Plan plan = accountService.createAccount("adminid").getPlan();
		plan.setEnableHistory(true);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		
		RequestRelay request = new RequestRelay();
		request.setValue("history#adminid");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(true);
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_history() throws Exception {
		//given
		Plan plan = accountService.createAccount("adminid").getPlan();
		plan.setEnableHistory(false);
		planService.updatePlan(PlanType.DEFAULT.name(), plan);
		
		RequestRelay request = new RequestRelay();
		request.setValue("history#adminid");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "History is not available");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(content().string(containsString(jsonResponse)));
	}
	
	@Test
	void shouldNotBeOkWhenValidate_accountNotFound() throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setNumeric("0");
		
		ResponseRelay response = new ResponseRelay(false, "404 NOT_FOUND \"Account not found\"");
		String jsonResponse = GSON.toJson(response);
		String jsonRequest;
		
		final String[] features = new String[] {
				"domain#NOT_FOUND",
				"group#NOT_FOUND",
				"switcher#NOT_FOUND",
				"component#NOT_FOUND",
				"environment#NOT_FOUND",
				"team#NOT_FOUND",
				"metrics#NOT_FOUND",
				"history#NOT_FOUND"
		};
		
		//test
		for (String feature : features) {
			request.setValue(feature);
			jsonRequest = GSON.toJson(request);
			this.mockMvc.perform(post("/switcher/v1/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer relay_token")
				.content(jsonRequest))
				.andExpect(status().is5xxServerError())
				.andExpect(content().string(containsString(jsonResponse)));
		}
	}
	
	@Test
	void shouldNotBeOkWhenValidate_invalidFeatureName() throws Exception {
		//given
		RequestRelay request = new RequestRelay();
		request.setValue("INVALID_FEATURE#adminid");
		request.setNumeric("0");
		String jsonRequest = GSON.toJson(request);
		
		ResponseRelay response = new ResponseRelay(false, "Invalid arguments - value INVALID_FEATURE#adminid - numeric 0");
		String jsonResponse = GSON.toJson(response);
		
		//test
		this.mockMvc.perform(post("/switcher/v1/validate")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer relay_token")
			.content(jsonRequest))
			.andDo(print())
			.andExpect(status().is5xxServerError())
			.andExpect(content().string(containsString(jsonResponse)));
	}

}
