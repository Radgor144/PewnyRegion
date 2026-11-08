
package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.VariableExternalIdEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface VariableExternalIdRepository extends ReactiveCrudRepository<VariableExternalIdEntity, Long> {

    Flux<VariableExternalIdEntity> findAllByVariableIdInAndProvider(
            Collection<Integer> variableIds,
            String provider
    );

    Flux<VariableExternalIdEntity> findAllByProvider(String provider);
}
