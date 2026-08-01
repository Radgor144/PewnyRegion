package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;

public record ImportChunk(CountyEntity county, List<Integer> varIds, List<Integer> years) {
    public boolean isTargeted() {
        return !CollectionUtils.isEmpty(years);
    }
}
