package com.pewnyregion.region.data.service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class BdlVariableDto {
    private String apiName;
    private List<Integer> varIds;
}