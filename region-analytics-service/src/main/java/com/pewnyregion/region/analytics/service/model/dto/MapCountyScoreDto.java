package com.pewnyregion.region.analytics.service.model.dto;

public record MapCountyScoreDto(
        String countyId,
        String countyName,
        Double score
) {}