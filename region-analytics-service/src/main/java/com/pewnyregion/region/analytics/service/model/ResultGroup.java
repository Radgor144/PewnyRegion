package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record ResultGroup(int id,
                          List<AnalyticsValueDto> values) {
}