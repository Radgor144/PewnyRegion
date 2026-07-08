package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.model.consts.VariableDirection;

import java.util.List;

public record VariableResponse(
        String apiName,
        List<Integer> bdlIds,
        VariableDirection direction
) {}