package com.pewnyregion.region.data.service.model;

import com.pewnyregion.region.data.service.model.consts.VariableDirection;

public record VariableResponse(String apiName,
                               String category,
                               VariableDirection direction,
                               boolean per_capita,
                               String unit) {
}