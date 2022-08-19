package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.PlanV2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.service.AccountService.PLAN_NOT_FOUND;
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
        givenPlan("PLAN_2");

        var account = accountService.getAccountByAdminId("adminid");
        assertEquals(PlanType.DEFAULT.toString(), account.getPlanV2().getName());

        //test
        accountService.updateAccountPlanV2("adminid", "PLAN_2");
        account = accountService.getAccountByAdminId("adminid");
        assertEquals("PLAN_2", account.getPlanV2().getName());
    }

    @Test
    void shouldNotUpdateAccount() {
        //given
        final var plan = "INVALID_PLAN";
        givenAccount();

        //test
        var exception = assertThrows(ResponseStatusException.class,
                () -> accountService.updateAccountPlanV2("adminid", plan));
        assertEquals(exception.getReason(), String.format(PLAN_NOT_FOUND, plan));
    }

    private void givenAccount() {
        accountService.createAccountV2("adminid");
    }

    private void givenPlan(String planName) {
        planService.createPlanV2(PlanV2.builder()
                .name(planName)
                .attributes(List.of(PlanAttribute.builder()
                        .feature("feature")
                        .value(true)
                        .build()))
                .build());
    }

}
