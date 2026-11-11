package com.pewnyregion.region.data.service.model;

public record TerritorialUnitScoreWithAverage(Integer variableId,
                                              Integer year,
                                              Double rawValue,
                                              Double averageScore) {
}