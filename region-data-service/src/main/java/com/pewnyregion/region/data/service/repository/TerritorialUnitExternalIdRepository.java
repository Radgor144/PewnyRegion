package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TerritorialUnitExternalIdRepository extends ReactiveCrudRepository<TerritorialUnitExternalIdEntity, Long> {

    Flux<TerritorialUnitExternalIdEntity> findAllByTerritorialUnitId(
            Long territorialUnitId
    );

    @Modifying
    @Query("""
            INSERT INTO territorial_unit_external_ids (
                territorial_unit_id,
                provider,
                external_id,
                provider_level
            )
            VALUES (
                :territorialUnitId,
                :provider,
                :externalId,
                :providerLevel
            )
            ON CONFLICT (provider, external_id)
            DO UPDATE SET
                territorial_unit_id = EXCLUDED.territorial_unit_id,
                provider_level = EXCLUDED.provider_level
            """)
    Mono<Integer> upsert(@Param("territorialUnitId") Long territorialUnitId,
                         @Param("provider") String provider,
                         @Param("externalId") String externalId,
                         @Param("providerLevel") String providerLevel);

    @Query("""
            SELECT external_mapping.*
            FROM territorial_unit_external_ids external_mapping
            JOIN territorial_units unit
                ON unit.id = external_mapping.territorial_unit_id
            WHERE external_mapping.provider = :provider
              AND unit.unit_level = :unitLevel
              AND unit.active = TRUE
            ORDER BY unit.teryt_code
            """)
    Flux<TerritorialUnitExternalIdEntity> findAllByProviderAndUnitLevel(@Param("provider") String provider,
                                                                        @Param("unitLevel") String unitLevel);

    @Query("""
            SELECT COUNT(*)
            FROM territorial_unit_external_ids mapping
            JOIN territorial_units unit
                ON unit.id = mapping.territorial_unit_id
            WHERE mapping.provider = :provider
              AND unit.unit_level = :unitLevel
              AND unit.active = TRUE
            """)
    Mono<Long> countByProviderAndUnitLevel(@Param("provider") String provider,
                                           @Param("unitLevel") String unitLevel);
}
