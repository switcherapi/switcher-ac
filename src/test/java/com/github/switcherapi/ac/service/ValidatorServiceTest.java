package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.FeaturePayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ValidatorServiceTest {

    @Autowired ValidatorService validatorService;
    @Autowired AccountService accountService;

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
        accountService.createAccount(owner);
        var request = FeaturePayload.builder()
                .feature(feature)
                .owner(owner)
                .total(total)
                .build();

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
        accountService.createAccount(owner);
        var request = FeaturePayload.builder()
                .feature(feature)
                .owner(owner)
                .total(total)
                .build();

        //test
        var responseRelayDTO = validatorService.execute(request);
        assertFalse(responseRelayDTO.isResult());
    }
}
