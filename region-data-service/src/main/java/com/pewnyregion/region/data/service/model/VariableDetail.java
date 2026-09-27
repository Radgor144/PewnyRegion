package com.pewnyregion.region.data.service.model;

import java.util.List;

public record VariableDetail(int bdlVariableId, List<YearlyData> yearlyValues) {
}
