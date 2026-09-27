package com.pewnyregion.region.data.service.model;

public record CountyScoreWithAverage(Integer bdlVariableId,
                                     Integer year,
                                     Double rawValue,
                                     Double averageScore) {
}