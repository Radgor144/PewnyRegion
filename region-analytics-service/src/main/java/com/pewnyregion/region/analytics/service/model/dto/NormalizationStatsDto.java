package com.pewnyregion.region.analytics.service.model.dto;

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