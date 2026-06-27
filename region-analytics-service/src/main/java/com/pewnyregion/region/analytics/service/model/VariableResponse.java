package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record VariableResponse(
        String apiName,
        List<Integer> bdlIds,
        VariableDirection direction
) {}