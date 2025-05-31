package com.github.switcherapi.ac.controller.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import com.github.switcherapi.ac.model.dto.RequestRelayDTO;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.service.AccountService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

public class ControllerTestUtils {

    @Autowired protected WebTestClient webTestClient;
    @Autowired protected AccountService accountService;

    protected final Gson gson = new Gson();

    protected void assertValidate(String adminId, String featureName,
                                  Integer value, ResponseRelayDTO expectedResponse, int expectedStatus) {
        //given
        var feature = FeaturePayload.builder()
                .feature(featureName)
                .owner(adminId)
                .total(value)
                .build();

        var request = new RequestRelayDTO(null, gson.toJson(feature));

        //test
        webTestClient.post()
                .uri("/switcher/v1/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
                .bodyValue(gson.toJson(request))
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody(String.class)
                .consumeWith(response ->
                        assertThat(response.getResponseBody()).contains(gson.toJson(expectedResponse)));
    }

    protected void assertLimiter(String value, ResponseRelayDTO expectedResponse,
                                   int expectedStatus) {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/switcher/v1/limiter")
                        .queryParam("value", value)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer relay_token")
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody(String.class)
                .consumeWith(response ->
                        assertThat(response.getResponseBody()).contains(gson.toJson(expectedResponse)));
    }

    protected String givenRequest(String adminId) {
        var request = new RequestRelayDTO(adminId, null);
        return gson.toJson(request);
    }

    protected void givenAccount(String adminId) {
        givenAccount(adminId, PlanType.DEFAULT.toString());
    }

    protected void givenAccount(String adminId, String plan) {
        accountService.createAccount(adminId, plan).block();
    }

    protected void assertDtoResponse(Plan planObj, String response)
            throws JsonProcessingException {
        var planDto = new ObjectMapper().readValue(response, PlanDTO.class);
        assertThat(planDto.name()).isEqualTo(planObj.getName());
    }

}
