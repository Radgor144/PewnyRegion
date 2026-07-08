package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.VariableResponse;
import com.pewnyregion.region.analytics.service.model.consts.VariableDirection;
import com.pewnyregion.region.analytics.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.analytics.service.repository.BdlVariableRepository;
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

    private final BdlVariableRepository variableRepository;
    private final BdlVariableIdRepository variableIdRepository;

    public Flux<VariableResponse> getAllVariables() {
        return variableRepository.findAll()
                                 .collectList()
                                 .filter(x -> !x.isEmpty())
                                 .flatMapMany(this::toVariableResponses);
    }

    public Mono<List<Integer>> getAllVariableIds() {
        return variableIdRepository.findAll()
                                   .map(BdlVariableIdEntity::getBdlId)
                                   .collectList();
    }

    public Mono<List<Integer>> getVariableIdsByApiNames(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .map(BdlVariableEntity::getId)
                                 .collectList()
                                 .filter(ids -> !ids.isEmpty())
                                 .flatMapMany(variableIdRepository::findByBdlVariableIdIn)
                                 .map(BdlVariableIdEntity::getBdlId)
                                 .collectList();
    }

    private Flux<VariableResponse> toVariableResponses(List<BdlVariableEntity> variables) {
        List<Integer> variableIds = variables.stream()
                                             .map(BdlVariableEntity::getId)
                                             .toList();

        return variableIdRepository.findByBdlVariableIdIn(variableIds)
                                   .collectMultimap(BdlVariableIdEntity::getBdlVariableId, BdlVariableIdEntity::getBdlId)
                                   .flatMapMany(idsByVariable -> Flux.fromIterable(variables)
                                                                     .map(entity -> mapToVariableResponse(idsByVariable, entity)));
    }

    private VariableResponse mapToVariableResponse(Map<Integer, Collection<Integer>> idsByVariable, BdlVariableEntity entity) {
        return new VariableResponse(entity.getApiName(),
                                    new ArrayList<>(idsByVariable.getOrDefault(entity.getId(), List.of())),
                                    VariableDirection.valueOf(entity.getDirection())
        );
    }
}