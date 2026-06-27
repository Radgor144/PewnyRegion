package com.pewnyregion.region.analytics.service.model;

public record RawDataStatsDto(
        String countyId,
        Integer variableId,
        Integer year,
        Double value,
        Double meanVal,
        Double stddevVal,
        String direction
) {}