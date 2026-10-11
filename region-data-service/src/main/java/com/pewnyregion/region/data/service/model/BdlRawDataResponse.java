package com.pewnyregion.region.data.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BdlRawDataResponse(String unitId,
                                 String unitName,
                                 List<ResultGroup> results) {
}