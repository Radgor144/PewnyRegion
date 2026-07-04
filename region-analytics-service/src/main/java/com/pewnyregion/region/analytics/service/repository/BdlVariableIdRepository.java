package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlVariableIdEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BdlVariableIdRepository extends ReactiveCrudRepository<BdlVariableIdEntity, Integer> {
    Flux<BdlVariableIdEntity> findByBdlVariableId(Integer bdlVariableId);
}