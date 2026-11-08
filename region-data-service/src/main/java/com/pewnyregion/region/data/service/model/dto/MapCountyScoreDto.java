package com.pewnyregion.region.data.service.model.dto;

public record MapCountyScoreDto(
        String terytCode,
        String countyName,
        Double score
) {}