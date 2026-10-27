package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.BdlVariableIdEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface BdlVariableIdRepository extends ReactiveCrudRepository<BdlVariableIdEntity, Integer> {
    Flux<BdlVariableIdEntity> findByBdlVariableIdIn(Collection<Integer> bdlVariableIds);
}