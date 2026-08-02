package com.pewnyregion.region.data.service.model.dto;

public record MapCountyScoreDto(
        String countyId,
        String countyName,
        Double score
) {}