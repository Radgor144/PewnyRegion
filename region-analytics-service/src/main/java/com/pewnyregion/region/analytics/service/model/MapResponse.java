package com.pewnyregion.region.analytics.service.model;

public record MapResponse(
        String countyId,
        String countyName,
        Double score
) {}