package com.pewnyregion.region.analytics.service.repository;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CountyRepository extends R2dbcRepository<CountyEntity, String> {

    Mono<CountyEntity> findByTerytCode(String terytCode);

    @Modifying
    @Query("""
        INSERT INTO counties (id, name, parent_id, level, teryt_code)
        VALUES (:id, :name, :parentId, :level, :terytCode)
        ON CONFLICT (id) DO UPDATE SET
            name       = EXCLUDED.name,
            parent_id  = EXCLUDED.parent_id,
            level      = EXCLUDED.level,
            teryt_code = EXCLUDED.teryt_code
    """)
    Mono<Void> upsertCounty(String id, String name, String parentId, Integer level, String terytCode);
}