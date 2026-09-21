package com.pewnyregion.region.data.service.model;

import java.util.List;

public record CountyDetailsResponse(String countyId, List<VariableDetail> variables) {
}
