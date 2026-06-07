package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.BdlVariableEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface BdlVariableRepository extends ReactiveCrudRepository<BdlVariableEntity, Integer> {
    Flux<BdlVariableEntity> findByApiNameIn(List<String> apiNames);
}