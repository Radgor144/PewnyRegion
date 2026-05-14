package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record Population(int id, int measureUnitId,
                         String lastUpdate,
                         List<PopulationValue> values) {
}