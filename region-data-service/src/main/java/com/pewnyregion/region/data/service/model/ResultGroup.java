package com.pewnyregion.region.data.service.model;

import com.pewnyregion.region.data.service.model.dto.AnalyticsValueDto;

import java.util.List;

public record ResultGroup(int id,
                          List<AnalyticsValueDto> values) {
}