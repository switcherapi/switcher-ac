package com.github.switcherapi.ac.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanAttribute {

    private String feature;

    private Object value;

    public boolean validate(Object request) {
        if (request instanceof Integer requestObj &&
                value instanceof Integer attributeObj) {
            return attributeObj <= requestObj;
        }

        if (request instanceof Boolean requestObj &&
                value instanceof Boolean attributeObj) {
            return Objects.equals(attributeObj, requestObj);
        }

        return false;
    }

}
