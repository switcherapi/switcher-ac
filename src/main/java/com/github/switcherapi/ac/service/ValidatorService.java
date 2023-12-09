package com.github.switcherapi.ac.service;

import com.github.switcherapi.ac.model.domain.Account;
import com.github.switcherapi.ac.model.domain.FeaturePayload;
import com.github.switcherapi.ac.model.dto.ResponseRelayDTO;
import com.github.switcherapi.ac.repository.AccountDao;
import com.github.switcherapi.ac.service.validator.AbstractValidatorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

import static com.github.switcherapi.ac.service.validator.SwitcherValidatorParams.*;
import static com.github.switcherapi.ac.util.Constants.*;

@Service
public class ValidatorService extends AbstractValidatorService {

    protected ValidatorService(AccountDao accountDao) {
        super(accountDao);
    }

    @Override
    protected ResponseRelayDTO executeValidator(final Account account) {
        final var maxPlanValue = account.getPlan().getAttributes().stream()
                .filter(attrib -> attrib.getFeature().equals(getParam(FEATURE)))
                .findFirst();

        if (maxPlanValue.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_INVALID_FEATURE.getValue());

        final var value = maxPlanValue.get().getValue();
        if (validate(value))
            return new ResponseRelayDTO(false, MSG_FEATURE_LIMIT_REACHED.getValue());

        return new ResponseRelayDTO(true);
    }

    @Override
    protected void validateRequest(FeaturePayload request) {
        try {
            Assert.notNull(request.getFeature(), MSG_FEATURE_MISSING.getValue());
            Assert.notNull(request.getOwner(), MSG_OWNER_MISSING.getValue());

            params.put(ADMINID, request.getOwner());
            params.put(TOTAL, request.getTotal());
            params.put(FEATURE, request.getFeature());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private boolean validate(Object value) {
        if (value instanceof Boolean) {
            final var boolValue = Boolean.parseBoolean(value.toString());
            return Boolean.FALSE.equals(boolValue);
        }

        if (value instanceof Integer) {
            final var intValue = Integer.parseInt(value.toString());
            return validate(intValue, getParam(TOTAL, Integer.class));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, MSG_PLAN_INVALID_VALUE.getValue());
    }

}
