package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static com.github.switcherapi.ac.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ValidatorServiceTest {

    @Autowired ValidatorService validatorService;
    @Autowired AccountService accountService;
    @Autowired PlanService planService;

    static Stream<Arguments> validatorInput() {
        return Stream.of(
                Arguments.of("domain", 0),
                Arguments.of("group", 1),
                Arguments.of("switcher", 2),
                Arguments.of("environment", 1),
                Arguments.of("component", 1),
                Arguments.of("team", 0),
                Arguments.of("rate_limit", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("validatorInput")
    void shouldValidateFromRequest(String feature, Integer total) {
        //given
        givenAccount("adminid_validate");
        var request = givenRequest(feature, "adminid_validate", total);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertTrue(responseRelayDTO.isResult());
    }

    static Stream<Arguments> validatorLimitInput() {
        return Stream.of(
                Arguments.of("domain", 1),
                Arguments.of("group", 2),
                Arguments.of("switcher", 3),
                Arguments.of("environment", 2),
                Arguments.of("component", 2),
                Arguments.of("team", 1),
                Arguments.of("history", null),
                Arguments.of("metrics", null),
                Arguments.of("rate_limit", 100)
        );
    }

    @ParameterizedTest
    @MethodSource("validatorLimitInput")
    void shouldNotValidateFromRequest(String feature, Integer total) {
        //given
        givenAccount("adminid_not_validate");
        var request = givenRequest(feature, "adminid_not_validate", total);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertFalse(responseRelayDTO.isResult());
        assertEquals(MSG_FEATURE_LIMIT_REACHED.getValue(), responseRelayDTO.getMessage());
    }

    @Test
    void shouldNotValidateFromRequest_invalidFeature() {
        //given
        givenAccount("adminid_invalid_feature");
        var request = givenRequest("invalid_feature", "adminid_invalid_feature", 1);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorService.execute(request));
        assertEquals(MSG_INVALID_FEATURE.getValue(), exception.getReason());
    }

    @Test
    void shouldNotValidateFromRequest_emptyFeature() {
        //given
        givenAccount("adminid_empty_feature");
        var request = givenRequest(null, "adminid_empty_feature", 1);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorService.execute(request));
        assertEquals(MSG_FEATURE_MISSING.getValue(), exception.getReason());
    }

    @Test
    void shouldValidateFromRequest_undeterminedValue() {
        //given
        givenPlan("PLAN_TEST_UNDETERMINED", "feature_undetermined", -1);
        givenAccount("adminid_undetermined", "PLAN_TEST_UNDETERMINED");
        var request = givenRequest("feature_undetermined", "adminid_undetermined", 999);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertTrue(responseRelayDTO.isResult());
    }

    @Test
    void shouldNotValidateFromRequest_invalidPlanValueType() {
        //given
        givenPlan("PLAN_TEST", "feature", 1.5);
        givenAccount("adminid", "PLAN_TEST");
        var request = givenRequest("feature", "adminid", null);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorService.execute(request));
        assertEquals(MSG_PLAN_INVALID_VALUE.getValue(), exception.getReason());
    }

    private FeaturePayload givenRequest(String feature, String owner, Integer total) {
        return FeaturePayload.builder()
                .feature(feature)
                .owner(owner)
                .total(total)
                .build();
    }

    private void givenAccount(String adminId) {
        givenAccount(adminId, null);
    }

    private void givenAccount(String adminId, String planName) {
        if (planName != null)
            accountService.createAccount(adminId, planName);
        else
            accountService.createAccount(adminId);
    }

    private void givenPlan(String planName, String featureName, Object value) {
        planService.createPlan(Plan.builder()
            .name(planName)
            .attributes(List.of(PlanAttribute.builder()
                    .feature(featureName)
                    .value(value)
                    .build()))
            .build());
    }
}
