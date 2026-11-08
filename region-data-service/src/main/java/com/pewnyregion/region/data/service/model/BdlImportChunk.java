package com.pewnyregion.region.data.service.model;

import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;

import java.util.List;

public record BdlImportChunk(TerritorialUnitExternalIdEntity unitMapping, List<VariableExternalIdEntity> variableMappings) {
}

