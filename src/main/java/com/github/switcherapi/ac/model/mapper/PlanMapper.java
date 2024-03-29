package com.github.switcherapi.ac.model.mapper;

import com.github.switcherapi.ac.model.domain.Feature;
import com.github.switcherapi.ac.model.domain.Plan;
import com.github.switcherapi.ac.model.domain.PlanAttribute;
import com.github.switcherapi.ac.model.dto.PlanDTO;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Generated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlanMapper {

    public static <T> List<PlanDTO> createCopy(List<T> from) {
        return from.stream()
                .map(item -> DefaultMapper.createCopy(item, PlanDTO.class))
                .toList();
    }

    public static PlanDTO createCopy(Plan from) {
        var to = DefaultMapper.createCopy(from, PlanDTO.class);
        var attributes = new ArrayList<PlanAttribute>();
        from.getAttributes().forEach(planAttribute ->
                attributes.add(DefaultMapper.createCopy(planAttribute, PlanAttribute.class))
        );

        to.setAttributes(attributes);
        return to;
    }

    public static void copyProperties(Plan from, Plan to) {
        DefaultMapper.copyProperties(from, to, "attributes");
        from.getAttributes().forEach(planAttribute -> {
            var feature = Feature.getFeatureEnum(planAttribute.getFeature());

            if (to.hasFeature(feature)) {
                to.getFeature(feature).setValue(planAttribute.getValue());
            } else {
                to.addFeature(feature, planAttribute.getValue());
            }
        });
    }
}
