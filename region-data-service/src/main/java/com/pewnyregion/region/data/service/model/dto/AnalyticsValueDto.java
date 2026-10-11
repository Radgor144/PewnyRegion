package com.pewnyregion.region.data.service.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyticsValueDto(int year,
                                Double val) {
}
