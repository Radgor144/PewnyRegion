package com.pewnyregion.region.data.service.model;

import com.pewnyregion.region.data.service.model.consts.VariableDirection;

import java.util.List;

public record VariableResponse(
        String apiName,
        List<Integer> bdlIds,
        VariableDirection direction,
        boolean per_capita
) {}