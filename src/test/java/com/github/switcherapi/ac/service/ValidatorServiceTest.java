package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanV2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ValidatorServiceTest {

    @Autowired ValidatorService validatorService;
    @Autowired AccountService accountService;
    @Autowired PlanService planService;

    static Stream<Arguments> validatorInput() {
        return Stream.of(
                Arguments.of("domain", "adminid", 0),
                Arguments.of("group", "adminid", 1),
                Arguments.of("switcher", "adminid", 2),
                Arguments.of("environment", "adminid", 1),
                Arguments.of("component", "adminid", 1),
                Arguments.of("team", "adminid", 0),
                Arguments.of("daily_execution", "adminid", 99)
        );
    }

    @ParameterizedTest
    @MethodSource("validatorInput")
    void shouldValidateFromRequest(String feature, String owner, Integer total) {
        //given
        givenAccount("adminid");
        var request = givenRequest(feature, owner, total);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertTrue(responseRelayDTO.isResult());
    }

    static Stream<Arguments> validatorLimitInput() {
        return Stream.of(
                Arguments.of("domain", "adminid", 1),
                Arguments.of("group", "adminid", 2),
                Arguments.of("switcher", "adminid", 3),
                Arguments.of("environment", "adminid", 2),
                Arguments.of("component", "adminid", 2),
                Arguments.of("team", "adminid", 1),
                Arguments.of("daily_execution", "adminid", 100),
                Arguments.of("enable_history", "adminid", null),
                Arguments.of("enable_metric", "adminid", null)
        );
    }

    @ParameterizedTest
    @MethodSource("validatorLimitInput")
    void shouldNotValidateFromRequest(String feature, String owner, Integer total) {
        //given
        givenAccount("adminid");
        var request = givenRequest(feature, owner, total);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertFalse(responseRelayDTO.isResult());
        assertEquals(ValidatorService.MSG_FEATURE_LIMIT_REACHED, responseRelayDTO.getMessage());
    }

    @Test
    void shouldNotValidateFromRequest_invalidFeature() {
        //given
        givenAccount("adminid");
        var request = givenRequest("invalid_feature", "adminid", 1);

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertFalse(responseRelayDTO.isResult());
        assertEquals(ValidatorService.MSG_INVALID_FEATURE, responseRelayDTO.getMessage());
    }

    @Test
    void shouldNotValidateFromRequest_emptyFeature() {
        //given
        givenAccount("adminid");
        var request = givenRequest(null, "adminid", 1);

        //test
        final var exception =
                assertThrows(ResponseStatusException.class, () -> validatorService.execute(request));
        assertEquals(ValidatorService.MSG_FEATURE_MISSING, exception.getReason());
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
        assertEquals(ValidatorService.MSG_PLAN_INVALID_VALUE, exception.getReason());
    }

    private FeaturePayload givenRequest(String feature, String owner, Integer total) {
        return FeaturePayload.builder()
                .feature(feature)
                .owner(owner)
                .total(total)
                .build();
    }

    private Account givenAccount(String adminId) {
        return givenAccount(adminId, null);
    }

    private Account givenAccount(String adminId, String planName) {
        if (planName != null)
            return accountService.createAccountV2(adminId, planName);
        return accountService.createAccountV2(adminId);
    }

    private void givenPlan(String planName, String featureName, Object value) {
        planService.createPlanV2(PlanV2.builder()
                .name(planName)
                .attributes(List.of(PlanAttribute.builder()
                        .feature(featureName)
                        .value(value)
                        .build()))
                .build());
    }
}
