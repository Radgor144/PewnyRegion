package com.pewnyregion.region.data.service.model;

import java.util.List;

public record CountyDetailsResponse(String terytCode, List<VariableDetail> variables) {
}
