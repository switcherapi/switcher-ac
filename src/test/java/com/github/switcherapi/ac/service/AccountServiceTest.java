package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.model.domain.Feature.DOMAIN;
import static com.github.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AccountServiceTest {

    @Autowired AccountService accountService;
    @Autowired PlanService planService;

    @Test
    void shouldUpdateAccount() {
        //given
        givenAccount();
        givenPlan();

        var account = accountService.getAccountByAdminId("adminid");
        assertEquals(PlanType.DEFAULT.toString(), account.getPlan().getName());

        //test
        accountService.updateAccountPlan("adminid", "PLAN_2");
        account = accountService.getAccountByAdminId("adminid");
        assertEquals("PLAN_2", account.getPlan().getName());
    }

    @Test
    void shouldNotUpdateAccount() {
        //given
        final var plan = "INVALID_PLAN";
        givenAccount();

        //test
        var exception = assertThrows(ResponseStatusException.class,
                () -> accountService.updateAccountPlan("adminid", plan));
        assertEquals(exception.getReason(), String.format(PLAN_NAME_NOT_FOUND.getValue(), plan));
    }

    private void givenAccount() {
        accountService.createAccount("adminid");
    }

    private void givenPlan() {
        planService.createPlan(Plan.builder()
                .name("PLAN_2")
                .attributes(List.of(PlanAttribute.builder()
                        .feature(DOMAIN.getValue())
                        .value(true)
                        .build()))
                .build());
    }

}
