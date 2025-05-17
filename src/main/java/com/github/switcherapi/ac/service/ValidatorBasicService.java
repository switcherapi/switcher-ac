package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.*;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.repository.PlanDao;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.*;
import static com.github.switcherapi.ac.util.Constants.*;

@Service
public class ValidatorBasicService extends AbstractValidatorService {

    protected ValidatorBasicService(AccountDao accountDao, PlanDao planDao) {
        super(accountDao, planDao);
    }

    @Override
    protected Mono<ResponseRelayDTO> executeValidator(final Account account) {
        return planDao.getPlanRepository().findById(account.getPlan())
                .flatMap(plan -> {
                    final var maxPlanValue = plan.getAttributes().stream()
                            .filter(attrib -> attrib.getFeature().equals(getParam(FEATURE)))
                            .findFirst().orElseGet(() -> PlanAttribute.builder().build());

                    final var value = maxPlanValue.getValue();
                    if (validate(value)) {
                        return Mono.just(ResponseRelayDTO.fail(MSG_FEATURE_LIMIT_REACHED.getValue()));
                    }

                    return Mono.just(ResponseRelayDTO.create(true));
                });
    }

    @Override
    protected void validateRequest(FeaturePayload request) {
        try {
            Assert.notNull(request.feature(), MSG_FEATURE_MISSING.getValue());
            Assert.notNull(request.owner(), MSG_OWNER_MISSING.getValue());
            Assert.notNull(Feature.getFeatureEnum(request.feature()), MSG_INVALID_FEATURE.getValue());

            params.put(ADMINID, request.owner());
            params.put(TOTAL, request.total());
            params.put(FEATURE, request.feature());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private boolean validate(Object value) {
        if (value instanceof Boolean) {
            final var boolValue = Boolean.parseBoolean(value.toString());
            return !boolValue;
        }

        if (value instanceof Integer) {
            final var intValue = Integer.parseInt(value.toString());
            return validate(intValue, getParam(TOTAL, Integer.class));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_PLAN_INVALID_VALUE.getValue());
    }

}
