package com.pewnyregion.region.analytics.service.model;

public record MapCountyScoreDto(
        String countyId,
        String countyName,
        Double score
) {}