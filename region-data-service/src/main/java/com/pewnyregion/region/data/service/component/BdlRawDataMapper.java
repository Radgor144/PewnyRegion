package com.pewnyregion.region.data.service.component;

import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import com.pewnyregion.region.data.service.model.BdlRawDataResponse;
import com.pewnyregion.region.data.service.model.StatisticalDataImportRecord;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BdlRawDataMapper {

    public List<StatisticalDataImportRecord> map(
            TerritorialUnitExternalIdEntity territorialUnitMapping,
            List<VariableExternalIdEntity> variableMappings,
            BdlRawDataResponse response
    ) {
        if (response.results() == null) {
            return List.of();
        }

        Map<Integer, Long> variableMappingIds = variableMappings.stream()
                                                                .collect(Collectors.toMap(
                                                                        mapping -> Integer.valueOf(mapping.getExternalId()),
                                                                        VariableExternalIdEntity::getId
                                                                ));

        return response.results().stream()
                       .flatMap(result -> {
                           Long variableExternalId = variableMappingIds.get(result.id());

                           if (variableExternalId == null) {
                               throw new IllegalStateException(
                                       "Missing BDL variable mapping for external ID: " + result.id()
                               );
                           }

                           return result.values().stream()
                                        .filter(value -> value.val() != null)
                                        .map(value -> new StatisticalDataImportRecord(
                                                territorialUnitMapping.getId(),
                                                variableExternalId,
                                                value.year(),
                                                value.val()
                                        ));
                       })
                       .toList();
    }
}