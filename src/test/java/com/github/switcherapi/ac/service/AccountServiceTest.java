package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.domain.PlanType;
import com.github.switcherapi.ac.model.domain.Plan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.github.switcherapi.ac.model.domain.Feature.DOMAIN;
import static com.github.switcherapi.ac.util.Constants.PLAN_NAME_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
class AccountServiceTest {

    @Autowired AccountService accountService;
    @Autowired PlanService planService;

    @Test
    void shouldUpdateAccount() {
        //given
        givenAccount();
        givenPlan();

        var account = accountService.getAccountByAdminId("adminid").block();
        var planDefault = planService.getPlanByName(PlanType.DEFAULT.toString()).block();
        assertNotNull(account);
        assertNotNull(planDefault);
        assertEquals(account.getPlan(), planDefault.getId());

        //test
        accountService.updateAccountPlan("adminid", "PLAN_2").block();
        var planTwo = planService.getPlanByName("PLAN_2").block();
        account = accountService.getAccountByAdminId("adminid").block();
        assertNotNull(account);
        assertNotNull(planTwo);
        assertEquals(account.getPlan(), planTwo.getId());
    }

    @Test
    void shouldNotUpdateAccount() {
        //given
        final var plan = "INVALID_PLAN";
        givenAccount();

        //test
        var updateAccount = accountService.updateAccountPlan("adminid", plan);
        var exception = assertThrows(ResponseStatusException.class, updateAccount::block);
        assertEquals(exception.getReason(), String.format(PLAN_NAME_NOT_FOUND.getValue(), plan));
    }

    private void givenAccount() {
        accountService.createAccount("adminid").block();
    }

    private void givenPlan() {
        planService.createPlan(Plan.builder()
                .name("PLAN_2")
                .attributes(List.of(PlanAttribute.builder()
                        .feature(DOMAIN.getValue())
                        .value(true)
                        .build()))
                .build()).block();
    }

}
