package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.consts.VariableDirection;
import com.pewnyregion.region.analytics.service.model.VariableResponse;
import com.pewnyregion.region.analytics.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.analytics.service.repository.BdlVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VariableService {

    private final BdlVariableRepository variableRepository;
    private final BdlVariableIdRepository variableIdRepository;

    public Flux<VariableResponse> getAllVariables() {
        return variableRepository.findAll()
                                 .flatMap(this::mapToResponse);
    }

    public Mono<List<Integer>> getAllVariableIds() {
        return variableIdRepository.findAll()
                                   .map(BdlVariableIdEntity::getBdlId)
                                   .collectList();
    }

    public Mono<List<Integer>> getVariableIdsByApiNames(List<String> apiNames) {
        return variableRepository.findByApiNameIn(apiNames)
                                 .flatMap(v -> variableIdRepository.findByBdlVariableId(v.getId()))
                                 .map(BdlVariableIdEntity::getBdlId)
                                 .collectList();
    }

    private Mono<VariableResponse> mapToResponse(BdlVariableEntity entity) {
//      ToDo: delete N+1 queries
        return variableIdRepository.findByBdlVariableId(entity.getId())
                                   .map(BdlVariableIdEntity::getBdlId)
                                   .collectList()
                                   .map(ids -> new VariableResponse(
                                           entity.getApiName(),
                                           ids,
                                           VariableDirection.valueOf(entity.getDirection())
                                   ));
    }
}