package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VariableExternalIdRepository extends R2dbcRepository<VariableExternalIdEntity, Long> {

    Mono<VariableExternalIdEntity> findByProviderAndExternalId(
            String provider,
            String externalId
    );

    Flux<VariableExternalIdEntity> findAllByVariableId(Integer variableId);

    Flux<VariableExternalIdEntity> findAllByVariableIdAndProvider(
            Integer variableId,
            String provider
    );

    Flux<VariableExternalIdEntity> findAllByProvider(String provider);
}