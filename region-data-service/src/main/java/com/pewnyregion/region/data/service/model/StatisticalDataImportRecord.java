package com.pewnyregion.region.data.service.model;

public record StatisticalDataImportRecord(
        Long territorialUnitExternalIdId,
        Long variableExternalIdId,
        Integer year,
        Double value
) {
}