package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record BdlRawDataResponse(String unitId,
                                 String unitName,
                                 List<ResultGroup> results) {
}