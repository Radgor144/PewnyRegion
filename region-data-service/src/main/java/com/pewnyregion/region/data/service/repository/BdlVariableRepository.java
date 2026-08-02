package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.BdlVariableEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface BdlVariableRepository extends ReactiveCrudRepository<BdlVariableEntity, Integer> {
    Flux<BdlVariableEntity> findByApiNameIn(List<String> apiNames);
}