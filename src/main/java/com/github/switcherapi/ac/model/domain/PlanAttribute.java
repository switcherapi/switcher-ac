package com.github.switcherapi.ac.model.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class PlanAttribute {

    private String feature;

    private Object value;

    public boolean validate(Object request) {
        if (request instanceof Integer && value instanceof Integer) {
            var requestObj = (Integer) request;
            var attributeObj = (Integer) value;
            return attributeObj <= requestObj;
        }

        if (request instanceof Boolean && value instanceof Boolean) {
            var requestObj = (Boolean) request;
            var attributeObj = (Boolean) value;
            return Objects.equals(attributeObj, requestObj);
        }

        return false;
    }

}
