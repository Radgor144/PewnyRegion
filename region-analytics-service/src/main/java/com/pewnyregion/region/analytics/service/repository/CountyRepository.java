package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CountyRepository extends R2dbcRepository<CountyEntity, String> {
    Flux<CountyEntity> findAllBy(Pageable pageable);
    Mono<CountyEntity> findByTerytCode(String terytCode);
}