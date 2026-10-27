package com.pewnyregion.region.data.service.model;

import java.util.List;

public record VariableDetail(String apiName, List<YearlyData> yearlyValues) {
}
