package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Feature;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static com.github.switcherapi.ac.model.domain.Feature.*;
import static com.github.switcherapi.ac.model.domain.PlanDefaults.*;
import static com.github.switcherapi.ac.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
class ValidatorBasicServiceTest {

    @Autowired
    ValidatorBasicService validatorBasicService;
    @Autowired AccountService accountService;
    @Autowired PlanService planService;

    static Stream<Arguments> validatorInput() {
        return Stream.of(
                Arguments.of(DOMAIN, DEFAULT_DOMAIN.getIntValue() - 1),
                Arguments.of(GROUP, DEFAULT_GROUP.getIntValue() - 1),
                Arguments.of(SWITCHER, DEFAULT_SWITCHER.getIntValue() - 1),
                Arguments.of(ENVIRONMENT, DEFAULT_ENVIRONMENT.getIntValue() - 1),
                Arguments.of(COMPONENT, DEFAULT_COMPONENT.getIntValue() - 1),
                Arguments.of(TEAM, DEFAULT_TEAM.getIntValue() - 1),
                Arguments.of(RATE_LIMIT, DEFAULT_RATE_LIMIT.getIntValue() - 1)
        );
    }

    @ParameterizedTest
    @MethodSource("validatorInput")
    void shouldValidateFromRequest(Feature feature, Integer total) {
        //given
        givenAccount("adminid_validate");
        var request = givenRequest(feature.getValue(), "adminid_validate", total);

        //test
        var responseRelayDTO = validatorBasicService.execute(request);
        assertTrue(responseRelayDTO.result());
    }

    static Stream<Arguments> validatorLimitInput() {
        return Stream.of(
                Arguments.of(DOMAIN, DEFAULT_DOMAIN.getIntValue()),
                Arguments.of(GROUP, DEFAULT_GROUP.getIntValue()),
                Arguments.of(SWITCHER, DEFAULT_SWITCHER.getIntValue()),
                Arguments.of(ENVIRONMENT, DEFAULT_ENVIRONMENT.getIntValue()),
                Arguments.of(COMPONENT, DEFAULT_COMPONENT.getIntValue()),
                Arguments.of(TEAM, DEFAULT_TEAM.getIntValue()),
                Arguments.of(HISTORY, null),
                Arguments.of(METRICS, null),
                Arguments.of(RATE_LIMIT, DEFAULT_RATE_LIMIT.getIntValue())
        );
    }

    @ParameterizedTest
    @MethodSource("validatorLimitInput")
    void shouldNotValidateFromRequest(Feature feature, Integer total) {
        //given
        givenAccount("adminid_not_validate");
        var request = givenRequest(feature.getValue(), "adminid_not_validate", total);

        //test
        var responseRelayDTO = validatorBasicService.execute(request);
        assertFalse(responseRelayDTO.result());
        assertEquals(MSG_FEATURE_LIMIT_REACHED.getValue(), responseRelayDTO.message());
    }

    @Test
    void shouldNotValidateFromRequest_invalidFeature() {
        //given
        givenAccount("adminid_invalid_feature");
        var request = givenRequest("invalid_feature", "adminid_invalid_feature", 1);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorBasicService.execute(request));
        assertEquals(MSG_INVALID_FEATURE.getValue() + ": invalid_feature", exception.getReason());
    }

    @Test
    void shouldNotValidateFromRequest_emptyFeature() {
        //given
        givenAccount("adminid_empty_feature");
        var request = givenRequest(null, "adminid_empty_feature", 1);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorBasicService.execute(request));
        assertEquals(MSG_FEATURE_MISSING.getValue(), exception.getReason());
    }

    @Test
    void shouldValidateFromRequest_undeterminedValue() {
        //given
        givenPlan("PLAN_TEST_UNDETERMINED", DOMAIN.getValue(), -1);
        givenAccount("adminid_undetermined", "PLAN_TEST_UNDETERMINED");
        var request = givenRequest(DOMAIN.getValue(), "adminid_undetermined", 999);

        //test
        var responseRelayDTO = validatorBasicService.execute(request);
        assertTrue(responseRelayDTO.result());
    }

    @Test
    void shouldNotValidateFromRequest_invalidPlanValueType() {
        //given
        givenPlan("PLAN_TEST", DOMAIN.getValue(), 1.5);
        givenAccount("adminid", "PLAN_TEST");
        var request = givenRequest(DOMAIN.getValue(), "adminid", null);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorBasicService.execute(request));
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
