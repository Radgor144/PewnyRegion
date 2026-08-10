package com.pewnyregion.region.data.service.model.dto;

public record NormalizationStatsDto(
        String countyId,
        Integer bdlVariableId,
        Integer year,
        Double rawValue,
        Double adjustedValue,
        String direction,
        Double percentile
) {}