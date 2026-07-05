package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.BdlVariableDto;
import com.pewnyregion.region.analytics.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.analytics.service.repository.BdlVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BdlVariableService {

    private final BdlVariableRepository bdlVariableRepository;
    private final BdlVariableIdRepository bdlVariableIdRepository;

    public Mono<List<Integer>> getAllRawVariableIds() {
        return bdlVariableIdRepository.findAll()
                .map(BdlVariableIdEntity::getBdlId)
                .collectList();
    }

    public Mono<List<Integer>> getRawVariableIdsByApiNames(List<String> apiNames) {
        return bdlVariableRepository.findByApiNameIn(apiNames)
                .flatMap(variable -> bdlVariableIdRepository.findByBdlVariableId(variable.getId()))
                .map(BdlVariableIdEntity::getBdlId)
                .collectList();
    }

    public Flux<BdlVariableDto> getAllVariablesWithIds() {
        return bdlVariableRepository.findAll()
                .flatMap(this::mapToVariableDto);
    }

    public Flux<BdlVariableDto> getVariablesByApiNames(List<String> apiNames) {
        return bdlVariableRepository.findByApiNameIn(apiNames)
                .flatMap(this::mapToVariableDto);
    }

    private Mono<BdlVariableDto> mapToVariableDto(BdlVariableEntity variable) {
        return bdlVariableIdRepository.findByBdlVariableId(variable.getId())
                .map(BdlVariableIdEntity::getBdlId)
                .collectList()
                .map(ids -> new BdlVariableDto(variable.getApiName(), ids));
    }
}