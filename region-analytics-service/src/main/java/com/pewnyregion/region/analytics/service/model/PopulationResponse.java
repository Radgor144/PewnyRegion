package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record PopulationResponse(int totalRecords,
                                 String unitId,
                                 String unitName,
                                 int aggregateId,
                                 List<Population> results) {
}

