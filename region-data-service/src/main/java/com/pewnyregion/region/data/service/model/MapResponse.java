package com.pewnyregion.region.data.service.model;

public record MapResponse(
        String countyId,
        String countyName,
        Double score
) {}