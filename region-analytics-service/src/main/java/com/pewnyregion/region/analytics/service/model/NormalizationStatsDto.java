package com.pewnyregion.region.analytics.service.model;

public record NormalizationStatsDto(
        String countyId,
        Integer bdlVariableId,
        Integer year,
        Double rawValue,
        Double adjustedValue,
        Double meanVal,
        Double stddevVal,
        String direction
) {}