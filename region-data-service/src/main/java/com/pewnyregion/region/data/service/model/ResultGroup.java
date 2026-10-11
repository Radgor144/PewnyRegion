package com.pewnyregion.region.data.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pewnyregion.region.data.service.model.dto.AnalyticsValueDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultGroup(int id,
                          List<AnalyticsValueDto> values) {
}