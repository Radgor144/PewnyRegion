package com.pewnyregion.region.analytics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BdlResponse(int totalRecords, int page, int pageSize, Links links, List<BdlUnit> results) {
}
