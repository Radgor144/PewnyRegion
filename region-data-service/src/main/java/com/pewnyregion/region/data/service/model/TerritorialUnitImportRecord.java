package com.pewnyregion.region.data.service.model;

import java.time.LocalDate;

public record TerritorialUnitImportRecord(String terytCode,
                                          String parentTerytCode,
                                          String name,
                                          String additionalName,
                                          String unitLevel,
                                          String rodz,
                                          LocalDate sourceStateAt) {
}