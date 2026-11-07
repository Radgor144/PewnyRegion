
package com.pewnyregion.region.data.service.service;

import com.pewnyregion.region.data.service.entity.VariableEntity;
import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import com.pewnyregion.region.data.service.model.VariableResponse;
import com.pewnyregion.region.data.service.model.consts.VariableDirection;
import com.pewnyregion.region.data.service.repository.VariableExternalIdRepository;
import com.pewnyregion.region.data.service.repository.VariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VariableService {

    private static final String BDL_PROVIDER = "BDL";

    private final VariableRepository variableRepository;
    private final VariableExternalIdRepository variableExternalIdRepository;

    public Flux<VariableResponse> getAllVariables() {
        return variableRepository.findAll()
                                 .collectList()
                                 .filter(variables -> !variables.isEmpty())
                                 .flatMapMany(this::toVariableResponses);
    }

    public Mono<List<Integer>> getAllBdlVariableIds() {
        return variableExternalIdRepository.findAllByProvider(BDL_PROVIDER)
                                           .map(this::toBdlId)
                                           .collectList();
    }

    public Mono<List<Integer>> getBdlVariableIdsByApiNames(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .map(VariableEntity::getId)
                                 .collectList()
                                 .filter(variableIds -> !variableIds.isEmpty())
                                 .flatMapMany(variableIds ->
                                         variableExternalIdRepository
                                                 .findAllByVariableIdInAndProvider(
                                                         variableIds,
                                                         BDL_PROVIDER
                                                 )
                                 )
                                 .map(this::toBdlId)
                                 .collectList();
    }

    private Flux<VariableResponse> toVariableResponses(List<VariableEntity> variables) {
        List<Integer> variableIds = variables.stream()
                                             .map(VariableEntity::getId)
                                             .toList();

        return variableExternalIdRepository
                .findAllByVariableIdInAndProvider(variableIds, BDL_PROVIDER)
                .collectMultimap(
                        VariableExternalIdEntity::getVariableId,
                        this::toBdlId
                )
                .flatMapMany(idsByVariable ->
                        Flux.fromIterable(variables)
                            .map(variable ->
                                    mapToVariableResponse(idsByVariable, variable)
                            )
                );
    }

    private VariableResponse mapToVariableResponse(
            Map<Integer, Collection<Integer>> idsByVariable,
            VariableEntity variable
    ) {
        return new VariableResponse(
                variable.getApiName(),
                variable.getCategory(),
                new ArrayList<>(idsByVariable.getOrDefault(variable.getId(), List.of())),
                VariableDirection.valueOf(variable.getDirection()),
                variable.isPerCapita(),
                variable.getUnit()
        );
    }

    private Integer toBdlId(VariableExternalIdEntity mapping) {
        try {
            return Integer.valueOf(mapping.getExternalId());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Invalid BDL variable external ID: " + mapping.getExternalId(),
                    exception
            );
        }
    }
}
