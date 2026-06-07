package com.pewnyregion.region.analytics.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class BdlVariableDto {
    private String apiName;
    private List<Integer> varIds;
}