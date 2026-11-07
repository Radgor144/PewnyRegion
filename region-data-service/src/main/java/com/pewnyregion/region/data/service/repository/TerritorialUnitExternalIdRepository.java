package com.pewnyregion.region.data.service.repository;

import com.pewnyregion.region.data.service.entity.TerritorialUnitExternalIdEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TerritorialUnitExternalIdRepository
        extends R2dbcRepository<TerritorialUnitExternalIdEntity, Long> {

    Mono<TerritorialUnitExternalIdEntity> findByProviderAndExternalId(
            String provider,
            String externalId
    );

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
    Mono<Integer> upsert(
            @Param("territorialUnitId") Long territorialUnitId,
            @Param("provider") String provider,
            @Param("externalId") String externalId,
            @Param("providerLevel") String providerLevel
    );

    Flux<TerritorialUnitExternalIdEntity> findAllByProvider(String provider);

    @Query("""
        SELECT external_mapping.*
        FROM territorial_unit_external_ids external_mapping
        JOIN territorial_units unit
            ON unit.id = external_mapping.territorial_unit_id
        WHERE external_mapping.provider = :provider
          AND unit.unit_level = :unitLevel
        ORDER BY unit.teryt_code
        """)
    Flux<TerritorialUnitExternalIdEntity> findAllByProviderAndUnitLevel(
            @Param("provider") String provider,
            @Param("unitLevel") String unitLevel
    );
}