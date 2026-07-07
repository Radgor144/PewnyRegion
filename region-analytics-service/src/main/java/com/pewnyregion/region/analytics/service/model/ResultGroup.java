package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.model.dto.AnalyticsValueDto;

import java.util.List;

public record ResultGroup(int id,
                          List<AnalyticsValueDto> values) {
}