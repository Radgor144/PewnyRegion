package com.pewnyregion.region.analytics.service.service;

import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import com.pewnyregion.region.analytics.service.model.BdlVariableDto;
import com.pewnyregion.region.analytics.service.repository.BdlVariableIdRepository;
import com.pewnyregion.region.analytics.service.repository.BdlVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BdlVariableService {

    private final BdlVariableRepository bdlVariableRepository;
    private final BdlVariableIdRepository bdlVariableIdRepository;

    public Flux<BdlVariableDto> getAllVariablesWithIds() {
        return bdlVariableRepository.findAll()
                .flatMap(variable -> bdlVariableIdRepository.findByBdlVariableId(variable.getId())
                        .map(BdlVariableIdEntity::getBdlId)
                        .collectList()
                        .map(ids -> new BdlVariableDto(variable.getApiName(), ids))
                );
    }

    public Flux<BdlVariableDto> getVariablesByApiNames(List<String> apiNames) {
        return bdlVariableRepository.findByApiNameIn(apiNames)
                .flatMap(variable -> bdlVariableIdRepository.findByBdlVariableId(variable.getId())
                        .map(BdlVariableIdEntity::getBdlId)
                        .collectList()
                        .map(ids -> new BdlVariableDto(variable.getApiName(), ids))
                );
    }
}