package com.pewnyregion.region.data.service.model;

import com.pewnyregion.region.data.service.entity.CountyEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;

public record ImportChunk(CountyEntity county, List<Integer> varIds, List<Integer> years) {
    public boolean isTargeted() {
        return !CollectionUtils.isEmpty(years);
    }
}
