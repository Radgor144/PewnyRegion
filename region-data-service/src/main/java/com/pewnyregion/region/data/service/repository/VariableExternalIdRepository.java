
package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface VariableExternalIdRepository extends ReactiveCrudRepository<VariableExternalIdEntity, Long> {

    Mono<VariableExternalIdEntity> findByProviderAndExternalId(
            String provider,
            String externalId
    );

    Flux<VariableExternalIdEntity> findAllByVariableId(Integer variableId);

    Flux<VariableExternalIdEntity> findAllByVariableIdAndProvider(
            Integer variableId,
            String provider
    );

    Flux<VariableExternalIdEntity> findAllByVariableIdInAndProvider(
            Collection<Integer> variableIds,
            String provider
    );

    Flux<VariableExternalIdEntity> findAllByProvider(String provider);
}
