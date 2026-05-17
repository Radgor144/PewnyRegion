package com.pewnyregion.region.analytics.service.model;

import lombok.Builder;

import java.util.List;

@Builder
public record AnalyticsResponse(String variableName,
                                String unitId,
                                String unitName,
                                List<AnalyticsValueDto> values) {
}
