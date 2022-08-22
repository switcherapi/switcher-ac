package com.github.switcherapi.ac.controller.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.ws.rs.core.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ControllerTestUtils {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected AccountService accountService;

    protected final Gson gson = new Gson();

    protected void assertValidate(String adminId, String featureName,
                                  Integer value, ResponseRelayDTO expectedResponse, int expectedStatus) throws Exception {
        //given
        var feature = FeaturePayload.builder()
                .feature(featureName)
                .owner(adminId)
                .total(value)
                .build();

        var request = new RequestRelayDTO();
        request.setPayload(gson.toJson(feature));

        //test
        this.mockMvc.perform(post("/switcher/v1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
                        .with(csrf())
                        .content(gson.toJson(request)))
                .andDo(print())
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(containsString(gson.toJson(expectedResponse))));
    }

    protected void assertExecution(String value, ResponseRelayDTO expectedResponse,
                                   int expectedStatus) throws Exception {
        this.mockMvc.perform(get("/switcher/v1/execution")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
                        .with(csrf())
                        .queryParam("value", value))
                .andDo(print())
                .andExpect(status().is(expectedStatus))
                .andExpect(content().string(containsString(gson.toJson(expectedResponse))));
    }

    protected String givenTrueAsExpectedResponse() {
        return givenExpectedResponse(true, null);
    }

    protected String givenExpectedResponse(boolean result, String message) {
        var response = new ResponseRelayDTO(result, message);
        return gson.toJson(response);
    }

    protected String givenRequest(String adminId) {
        var request = new RequestRelayDTO();
        request.setValue(adminId);
        return gson.toJson(request);
    }

    protected void givenAccount(String adminId) {
        givenAccount(adminId, PlanType.DEFAULT.toString());
    }

    protected void givenAccount(String adminId, String plan) {
        accountService.createAccount(adminId, plan);
    }

    protected void assertDtoResponse(Plan planObj, String response)
            throws JsonProcessingException {
        var planDto = new ObjectMapper().readValue(response, PlanDTO.class);
        assertThat(planDto.getName()).isEqualTo(planObj.getName());
    }

}
